package com.gym.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.mapper.toDomain
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleGroup
import com.gym.core.model.MuscleRegion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExercisesUiState(
    val searchQuery: String = "",
    val selectedRegion: MuscleRegion? = null,
    val selectedMuscleGroup: MuscleGroup? = null,
    val showExcluded: Boolean = false,
    val exercises: List<Exercise> = emptyList(),
    val regionCounts: Map<MuscleRegion, Int> = emptyMap(),
    val isLoading: Boolean = false
)

class ExercisesViewModel(
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedRegion = MutableStateFlow<MuscleRegion?>(null)
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

        // Подсчет упражнений по макро-регионам
        val counts = MuscleRegion.values().associateWith { r ->
            allDomain.count { it.region == r && (!it.isExcluded || showExcluded) }
        }

        val filtered = allDomain.filter { exercise ->
            val matchesExcluded = showExcluded || !exercise.isExcluded
            val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            val matchesRegion = region == null || exercise.region == region
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

    fun onRegionSelected(region: MuscleRegion?) {
        _selectedRegion.value = region
        _selectedMuscleGroup.value = null // Сброс подгруппы при смене хаба
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
}
