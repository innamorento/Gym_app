package com.gym.core.model

data class TemplateExercise(
    val id: Long = 0,
    val exercise: Exercise,
    val orderIndex: Int,
    val supersetId: String? = null,
    val targetSetsCount: Int = 3,
    val defaultRestSeconds: Int = 90
) {
    val isPartOfSuperset: Boolean
        get() = !supersetId.isNullOrBlank()
}

data class WorkoutTemplate(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val exercises: List<TemplateExercise> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null
)
