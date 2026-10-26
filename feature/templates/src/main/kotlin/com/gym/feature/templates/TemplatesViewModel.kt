package com.gym.feature.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.WorkoutTemplateEntity
import com.gym.core.database.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TemplatesViewModel(
    private val templateDao: TemplateDao
) : ViewModel() {

    val listUiState: StateFlow<TemplatesListUiState> = templateDao.getAllTemplatesWithExercises()
        .map { list ->
            TemplatesListUiState(
                isLoading = false,
                templates = list.map { it.toDomain() }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TemplatesListUiState(isLoading = true)
        )

    private val _editorUiState = MutableStateFlow(TemplateEditorUiState())
    val editorUiState: StateFlow<TemplateEditorUiState> = _editorUiState.asStateFlow()

    fun initEditor(templateId: Long = 0) {
        if (templateId == 0L) {
            _editorUiState.value = TemplateEditorUiState()
        } else {
            viewModelScope.launch {
                val existing = templateDao.getTemplateWithExercises(templateId)?.toDomain()
                if (existing != null) {
                    _editorUiState.value = TemplateEditorUiState(
                        templateId = existing.id,
                        title = existing.title,
                        description = existing.description.orEmpty(),
                        exercises = existing.exercises.map {
                            TemplateExerciseDraft(
                                exercise = it.exercise,
                                supersetId = it.supersetId,
                                targetSetsCount = it.targetSetsCount,
                                defaultRestSeconds = it.defaultRestSeconds
                            )
                        }
                    )
                }
            }
        }
    }

    fun handleIntent(intent: TemplateEditorIntent) {
        when (intent) {
            is TemplateEditorIntent.ChangeTitle -> {
                _editorUiState.update { it.copy(title = intent.title) }
            }
            is TemplateEditorIntent.ChangeDescription -> {
                _editorUiState.update { it.copy(description = intent.description) }
            }
            is TemplateEditorIntent.AddExercise -> {
                _editorUiState.update { state ->
                    state.copy(
                        exercises = state.exercises + TemplateExerciseDraft(
                            exercise = intent.exercise,
                            defaultRestSeconds = intent.exercise.defaultRestSeconds
                        )
                    )
                }
            }
            is TemplateEditorIntent.RemoveExercise -> {
                _editorUiState.update { state ->
                    state.copy(exercises = state.exercises.filterNot { it.tempId == intent.tempId })
                }
            }
            is TemplateEditorIntent.UpdateSetsCount -> {
                _editorUiState.update { state ->
                    state.copy(
                        exercises = state.exercises.map {
                            if (it.tempId == intent.tempId) it.copy(targetSetsCount = intent.count) else it
                        }
                    )
                }
            }
            is TemplateEditorIntent.UpdateRestSeconds -> {
                _editorUiState.update { state ->
                    state.copy(
                        exercises = state.exercises.map {
                            if (it.tempId == intent.tempId) it.copy(defaultRestSeconds = intent.seconds) else it
                        }
                    )
                }
            }
            is TemplateEditorIntent.LinkAsSuperset -> {
                val groupSupersetId = UUID.randomUUID().toString()
                _editorUiState.update { state ->
                    state.copy(
                        exercises = state.exercises.map { item ->
                            if (item.tempId == intent.firstTempId || item.tempId == intent.secondTempId) {
                                item.copy(supersetId = groupSupersetId)
                            } else {
                                item
                            }
                        }
                    )
                }
            }
            is TemplateEditorIntent.UnlinkFromSuperset -> {
                _editorUiState.update { state ->
                    state.copy(
                        exercises = state.exercises.map {
                            if (it.tempId == intent.tempId) it.copy(supersetId = null) else it
                        }
                    )
                }
            }
            is TemplateEditorIntent.SaveTemplate -> {
                saveTemplate()
            }
        }
    }

    private fun saveTemplate() {
        val state = _editorUiState.value
        if (state.title.isBlank() || state.exercises.isEmpty()) return

        viewModelScope.launch {
            _editorUiState.update { it.copy(isSaving = true) }

            val templateEntity = WorkoutTemplateEntity(
                id = state.templateId,
                title = state.title.trim(),
                description = state.description.trim().ifBlank { null }
            )

            val exerciseEntities = state.exercises.mapIndexed { index, draft ->
                TemplateExerciseEntity(
                    templateId = state.templateId,
                    exerciseId = draft.exercise.id,
                    orderIndex = index,
                    supersetId = draft.supersetId,
                    targetSetsCount = draft.targetSetsCount,
                    defaultRestSeconds = draft.defaultRestSeconds
                )
            }

            templateDao.saveTemplateWithExercises(templateEntity, exerciseEntities)
            _editorUiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            templateDao.deleteTemplate(id)
        }
    }
}
