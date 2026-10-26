package com.gym.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.mapper.toDomain
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ExercisesUiState(
    val searchQuery: String = "",
    val selectedMuscleGroup: MuscleGroup? = null,
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false
)

class ExercisesViewModel(
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroup?>(null)

    val uiState: StateFlow<ExercisesUiState> = combine(
        exerciseDao.getAllExercises(),
        _searchQuery,
        _selectedMuscleGroup
    ) { allEntities, query, muscleGroup ->
        val filtered = allEntities
            .map { it.toDomain() }
            .filter { exercise ->
                val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
                val matchesMuscle = muscleGroup == null || exercise.primaryMuscleGroup == muscleGroup
                matchesQuery && matchesMuscle
            }

        ExercisesUiState(
            searchQuery = query,
            selectedMuscleGroup = muscleGroup,
            exercises = filtered,
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

    fun onMuscleGroupSelected(group: MuscleGroup?) {
        _selectedMuscleGroup.value = group
    }
}
