package com.gym.core.model

data class Exercise(
    val id: Long = 0,
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: List<MuscleGroup> = emptyList(),
    val equipment: Equipment,
    val isCustom: Boolean = false,
    val defaultRestSeconds: Int = 90,
    val instructions: String? = null
)
