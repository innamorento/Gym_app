package com.gym.feature.workout.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.dao.WorkoutDao
import com.gym.core.database.mapper.toDomain
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleCategory
import com.gym.core.model.SetType
import com.gym.core.model.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AnalyticsTimeRange(val titleRu: String) {
    WEEK("7 дней"),
    MONTH("30 дней"),
    ALL_TIME("Всё время")
}

data class MuscleCategoryVolume(
    val category: MuscleCategory,
    val volumeKg: Double,
    val setsCount: Int,
    val percentage: Float
)

data class OneRmRecord(
    val workoutDate: Long,
    val weight: Double,
    val reps: Int,
    val estimated1Rm: Double
)

data class ExercisePrSummary(
    val exerciseId: Long,
    val exerciseName: String,
    val best1Rm: Double,
    val bestWeight: Double,
    val bestReps: Int,
    val records: List<OneRmRecord>
)

data class AnalyticsUiState(
    val selectedTimeRange: AnalyticsTimeRange = AnalyticsTimeRange.MONTH,
    val totalVolumeKg: Double = 0.0,
    val totalWorkoutsCount: Int = 0,
    val totalCompletedSets: Int = 0,
    val muscleBreakdown: List<MuscleCategoryVolume> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val selectedExerciseId: Long? = null,
    val selectedExercisePr: ExercisePrSummary? = null,
    val isLoading: Boolean = false
)

class AnalyticsViewModel(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(AnalyticsTimeRange.MONTH)
    private val _selectedExerciseId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<AnalyticsUiState> = combine(
        workoutDao.getCompletedWorkoutsForUser("default_user"),
        exerciseDao.getAllExercises(),
        _selectedTimeRange,
        _selectedExerciseId
    ) { completedWorkoutsDetails, exerciseEntities, timeRange, selectedExId ->
        val completedWorkouts = completedWorkoutsDetails.map { it.toDomain() }
        val allExercises = exerciseEntities.map { it.toDomain() }

        // Фильтрация тренировок по выбранному временному диапазону
        val now = System.currentTimeMillis()
        val rangeCutoff = when (timeRange) {
            AnalyticsTimeRange.WEEK -> now - 7L * 24 * 3600 * 1000
            AnalyticsTimeRange.MONTH -> now - 30L * 24 * 3600 * 1000
            AnalyticsTimeRange.ALL_TIME -> 0L
        }

        val filteredWorkouts = completedWorkouts.filter { it.startTime >= rangeCutoff }

        // Общие метрики
        val totalVolume = filteredWorkouts.sumOf { it.totalVolume }
        val totalWorkouts = filteredWorkouts.size
        val totalSets = filteredWorkouts.sumOf { it.totalCompletedSets }

        // Расчет объема по категориям мышц
        val muscleVolumeMap = mutableMapOf<MuscleCategory, Double>()
        val muscleSetsMap = mutableMapOf<MuscleCategory, Int>()

        for (workout in filteredWorkouts) {
            for (exercise in workout.exercises) {
                val category = MuscleCategory.categoryFor(exercise.exercise.primaryMuscleGroup)
                val workingSets = exercise.sets.filter { it.isCompleted && it.setType != SetType.WARMUP }
                val exVolume = workingSets.sumOf { it.volume }
                val exSetsCount = workingSets.size

                muscleVolumeMap[category] = (muscleVolumeMap[category] ?: 0.0) + exVolume
                muscleSetsMap[category] = (muscleSetsMap[category] ?: 0) + exSetsCount
            }
        }

        val muscleBreakdown = muscleVolumeMap.map { (cat, vol) ->
            val percentage = if (totalVolume > 0.0) ((vol / totalVolume) * 100).toFloat() else 0f
            MuscleCategoryVolume(
                category = cat,
                volumeKg = (vol * 10.0).roundToInt() / 10.0,
                setsCount = muscleSetsMap[cat] ?: 0,
                percentage = (percentage * 10).roundToInt() / 10f
            )
        }.sortedByDescending { it.volumeKg }

        // Упражнения, по которым есть выполненные тренировки
        val performedExerciseIds = completedWorkouts
            .flatMap { it.exercises }
            .map { it.exercise.id }
            .distinct()

        val availableExercises = allExercises.filter { it.id in performedExerciseIds }.ifEmpty {
            allExercises.take(10)
        }

        val effectiveExerciseId = selectedExId ?: availableExercises.firstOrNull()?.id

        val selectedPrSummary = if (effectiveExerciseId != null) {
            calculateExercisePrSummary(effectiveExerciseId, completedWorkouts, allExercises)
        } else {
            null
        }

        AnalyticsUiState(
            selectedTimeRange = timeRange,
            totalVolumeKg = (totalVolume * 10.0).roundToInt() / 10.0,
            totalWorkoutsCount = totalWorkouts,
            totalCompletedSets = totalSets,
            muscleBreakdown = muscleBreakdown,
            availableExercises = availableExercises,
            selectedExerciseId = effectiveExerciseId,
            selectedExercisePr = selectedPrSummary,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState(isLoading = true)
    )

    private fun calculateExercisePrSummary(
        exerciseId: Long,
        allCompletedWorkouts: List<Workout>,
        allExercises: List<Exercise>
    ): ExercisePrSummary? {
        val exercise = allExercises.find { it.id == exerciseId } ?: return null
        val records = mutableListOf<OneRmRecord>()

        var best1Rm = 0.0
        var bestWeight = 0.0
        var bestReps = 0

        // Проходим по хронологии тренировок
        for (workout in allCompletedWorkouts.sortedBy { it.startTime }) {
            val matchingExercises = workout.exercises.filter { it.exercise.id == exerciseId }
            var max1RmInWorkout = 0.0
            var bestSetInWorkoutWeight = 0.0
            var bestSetInWorkoutReps = 0

            for (we in matchingExercises) {
                for (set in we.sets.filter { it.isCompleted && it.setType != SetType.WARMUP }) {
                    val rm = set.estimated1RM
                    if (rm > max1RmInWorkout) {
                        max1RmInWorkout = rm
                        bestSetInWorkoutWeight = set.weight
                        bestSetInWorkoutReps = set.reps
                    }
                    if (rm > best1Rm) {
                        best1Rm = rm
                        bestWeight = set.weight
                        bestReps = set.reps
                    }
                }
            }

            if (max1RmInWorkout > 0.0) {
                records.add(
                    OneRmRecord(
                        workoutDate = workout.startTime,
                        weight = bestSetInWorkoutWeight,
                        reps = bestSetInWorkoutReps,
                        estimated1Rm = max1RmInWorkout
                    )
                )
            }
        }

        return ExercisePrSummary(
            exerciseId = exerciseId,
            exerciseName = exercise.name,
            best1Rm = (best1Rm * 10.0).roundToInt() / 10.0,
            bestWeight = bestWeight,
            bestReps = bestReps,
            records = records
        )
    }

    fun selectTimeRange(range: AnalyticsTimeRange) {
        _selectedTimeRange.value = range
    }

    fun selectExercise(exerciseId: Long) {
        _selectedExerciseId.value = exerciseId
    }
}
