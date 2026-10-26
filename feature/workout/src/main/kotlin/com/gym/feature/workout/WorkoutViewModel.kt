package com.gym.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.CoachAndReviewDao
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.dao.WorkoutDao
import com.gym.core.database.entity.SetVideoReviewEntity
import com.gym.core.database.entity.WorkoutEntity
import com.gym.core.database.entity.WorkoutExerciseEntity
import com.gym.core.database.entity.WorkoutSetEntity
import com.gym.core.database.mapper.toDomain
import com.gym.core.database.mapper.toEntity
import com.gym.core.model.PerceivedEffort
import com.gym.core.model.SetType
import com.gym.core.model.Workout
import com.gym.core.model.WorkoutStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WorkoutUiState {
    object Idle : WorkoutUiState
    data class Active(val workout: Workout) : WorkoutUiState
}

class WorkoutViewModel(
    private val workoutDao: WorkoutDao,
    private val templateDao: TemplateDao,
    private val coachAndReviewDao: CoachAndReviewDao
) : ViewModel() {

    fun attachVideoToSet(setId: Long, uriString: String) {
        viewModelScope.launch {
            coachAndReviewDao.attachVideoToSet(
                SetVideoReviewEntity(
                    setId = setId,
                    localVideoUri = uriString,
                    durationSeconds = 60
                )
            )
        }
    }

    fun submitCoachFeedback(reviewId: Long, setId: Long, uri: String, comment: String, rating: Int, coachName: String) {
        viewModelScope.launch {
            coachAndReviewDao.submitCoachReview(
                SetVideoReviewEntity(
                    id = reviewId,
                    setId = setId,
                    localVideoUri = uri,
                    feedbackComment = comment,
                    coachRating = rating,
                    coachName = coachName,
                    reviewedAt = System.currentTimeMillis()
                )
            )
        }
    }

    val uiState: StateFlow<WorkoutUiState> = workoutDao.getActiveWorkout()
        .map { workoutWithDetails ->
            if (workoutWithDetails != null) {
                WorkoutUiState.Active(workoutWithDetails.toDomain())
            } else {
                WorkoutUiState.Idle
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutUiState.Idle
        )

    fun startWorkoutFromTemplate(templateId: Long) {
        viewModelScope.launch {
            val templateWithExercises = templateDao.getTemplateWithExercises(templateId) ?: return@launch
            val template = templateWithExercises.toDomain()

            val workoutId = workoutDao.insertWorkout(
                WorkoutEntity(
                    templateId = template.id,
                    title = template.title,
                    startTime = System.currentTimeMillis(),
                    status = WorkoutStatus.IN_PROGRESS
                )
            )

            template.exercises.forEachIndexed { index, templateExercise ->
                val workoutExerciseId = workoutDao.insertWorkoutExercise(
                    WorkoutExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = templateExercise.exercise.id,
                        orderIndex = index,
                        supersetId = templateExercise.supersetId
                    )
                )

                // Создаем начальные подходы
                val sets = (1..templateExercise.targetSetsCount).map { setNumber ->
                    WorkoutSetEntity(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = setNumber,
                        setType = SetType.NORMAL,
                        weight = 0.0,
                        reps = 0,
                        isCompleted = false
                    )
                }
                workoutDao.insertWorkoutSets(sets)
            }
        }
    }

    fun toggleSetCompletion(setId: Long, currentIsCompleted: Boolean) {
        viewModelScope.launch {
            val newCompleted = !currentIsCompleted
            val completedTime = if (newCompleted) System.currentTimeMillis() else null
            workoutDao.updateSetCompletion(setId, newCompleted, completedTime)
        }
    }

    fun updateSetEffort(setId: Long, effort: PerceivedEffort) {
        viewModelScope.launch {
            workoutDao.updateSetEffort(setId, effort)
        }
    }

    fun addSetToExercise(workoutExerciseId: Long, currentSetsCount: Int) {
        viewModelScope.launch {
            workoutDao.insertWorkoutSet(
                WorkoutSetEntity(
                    workoutExerciseId = workoutExerciseId,
                    setNumber = currentSetsCount + 1,
                    setType = SetType.NORMAL,
                    weight = 0.0,
                    reps = 0,
                    isCompleted = false
                )
            )
        }
    }

    fun finishWorkout(workoutId: Long) {
        viewModelScope.launch {
            val currentWorkout = workoutDao.getWorkoutById(workoutId) ?: return@launch
            val now = System.currentTimeMillis()
            val duration = (now - currentWorkout.workout.startTime) / 1000

            workoutDao.updateWorkout(
                currentWorkout.workout.copy(
                    endTime = now,
                    durationSeconds = duration,
                    status = WorkoutStatus.COMPLETED
                )
            )
        }
    }
}
