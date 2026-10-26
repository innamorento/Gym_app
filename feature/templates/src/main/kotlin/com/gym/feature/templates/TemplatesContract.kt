package com.gym.feature.templates

import com.gym.core.model.Exercise
import com.gym.core.model.WorkoutTemplate

data class TemplateExerciseDraft(
    val tempId: String = java.util.UUID.randomUUID().toString(),
    val exercise: Exercise,
    val supersetId: String? = null,
    val targetSetsCount: Int = 3,
    val defaultRestSeconds: Int = 90
)

data class TemplatesListUiState(
    val isLoading: Boolean = false,
    val templates: List<WorkoutTemplate> = emptyList()
)

data class TemplateEditorUiState(
    val templateId: Long = 0,
    val title: String = "",
    val description: String = "",
    val exercises: List<TemplateExerciseDraft> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

sealed interface TemplateEditorIntent {
    data class ChangeTitle(val title: String) : TemplateEditorIntent
    data class ChangeDescription(val description: String) : TemplateEditorIntent
    data class AddExercise(val exercise: Exercise) : TemplateEditorIntent
    data class RemoveExercise(val tempId: String) : TemplateEditorIntent
    data class UpdateSetsCount(val tempId: String, val count: Int) : TemplateEditorIntent
    data class UpdateRestSeconds(val tempId: String, val seconds: Int) : TemplateEditorIntent
    data class LinkAsSuperset(val firstTempId: String, val secondTempId: String) : TemplateEditorIntent
    data class UnlinkFromSuperset(val tempId: String) : TemplateEditorIntent
    object SaveTemplate : TemplateEditorIntent
}
