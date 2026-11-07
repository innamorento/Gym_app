package com.gym.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.entity.ExerciseEntity
import com.gym.core.database.mapper.toDomain
import com.gym.core.model.Equipment
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleCategory
import com.gym.core.model.MuscleGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExercisesUiState(
    val searchQuery: String = "",
    val selectedRegion: MuscleCategory? = null,
    val selectedMuscleGroup: MuscleGroup? = null,
    val showExcluded: Boolean = false,
    val exercises: List<Exercise> = emptyList(),
    val regionCounts: Map<MuscleCategory, Int> = emptyMap(),
    val isLoading: Boolean = false
)

class ExercisesViewModel(
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedRegion = MutableStateFlow<MuscleCategory?>(null)
    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroup?>(null)
    private val _showExcluded = MutableStateFlow(false)

    val uiState: StateFlow<ExercisesUiState> = combine(
        exerciseDao.getAllExercises(),
        _searchQuery,
        _selectedRegion,
        _selectedMuscleGroup,
        _showExcluded
    ) { allEntities, query, region, muscleGroup, showExcluded ->
        val allDomain = allEntities.map { it.toDomain() }

        // Подсчет упражнений по понятным анатомическим категориям
        val counts = MuscleCategory.values().associateWith { c ->
            allDomain.count { it.category == c && (!it.isExcluded || showExcluded) }
        }

        val filtered = allDomain.filter { exercise ->
            val matchesExcluded = showExcluded || !exercise.isExcluded
            val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            val matchesRegion = region == null || exercise.category == region
            val matchesMuscle = muscleGroup == null || exercise.primaryMuscleGroup == muscleGroup

            matchesExcluded && matchesQuery && matchesRegion && matchesMuscle
        }

        ExercisesUiState(
            searchQuery = query,
            selectedRegion = region,
            selectedMuscleGroup = muscleGroup,
            showExcluded = showExcluded,
            exercises = filtered,
            regionCounts = counts,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExercisesUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onRegionSelected(region: MuscleCategory?) {
        _selectedRegion.value = region
        _selectedMuscleGroup.value = null
    }

    fun onMuscleGroupSelected(group: MuscleGroup?) {
        _selectedMuscleGroup.value = group
    }

    fun toggleShowExcluded() {
        _showExcluded.value = !_showExcluded.value
    }

    fun excludeExercise(exerciseId: Long, reason: String = "Нет в моем зале") {
        viewModelScope.launch {
            exerciseDao.updateExclusionStatus(exerciseId, isExcluded = true, reason = reason)
        }
    }

    fun restoreExercise(exerciseId: Long) {
        viewModelScope.launch {
            exerciseDao.updateExclusionStatus(exerciseId, isExcluded = false, reason = null)
        }
    }

    fun createCustomExercise(
        name: String,
        muscleGroup: MuscleGroup,
        equipment: Equipment,
        defaultRestSeconds: Int = 90,
        initialPosition: String? = null,
        execution: String? = null,
        commonMistakes: String? = null,
        onSuccess: (Long) -> Unit = {}
    ) {
        if (name.isBlank()) return

        val instructionsBuilder = StringBuilder()
        if (!initialPosition.isNullOrBlank()) {
            instructionsBuilder.append("📍 Исходное положение:\n").append(initialPosition.trim()).append("\n\n")
        }
        if (!execution.isNullOrBlank()) {
            instructionsBuilder.append("🔄 Выполнение движения:\n").append(execution.trim()).append("\n\n")
        }
        if (!commonMistakes.isNullOrBlank()) {
            instructionsBuilder.append("⚠️ Частые ошибки:\n").append(commonMistakes.trim())
        }

        val fullInstructions = instructionsBuilder.toString().trim().ifEmpty { null }

        viewModelScope.launch {
            val entity = ExerciseEntity(
                name = name.trim(),
                primaryMuscleGroup = muscleGroup,
                secondaryMuscleGroups = "",
                equipment = equipment,
                isCustom = true,
                defaultRestSeconds = defaultRestSeconds,
                instructions = fullInstructions,
                isExcluded = false,
                exclusionReason = null
            )
            val newId = exerciseDao.insertExercise(entity)
            onSuccess(newId)
        }
    }

    fun deleteCustomExercise(exerciseId: Long) {
        viewModelScope.launch {
            exerciseDao.deleteCustomExercise(exerciseId)
        }
    }
}
