package com.gym.core.model

data class Exercise(
    val id: Long = 0,
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: List<MuscleGroup> = emptyList(),
    val equipment: Equipment,
    val isCustom: Boolean = false,
    val defaultRestSeconds: Int = 90,
    val instructions: String? = null,
    val isExcluded: Boolean = false,
    val exclusionReason: String? = null
) {
    val category: MuscleCategory
        get() = MuscleCategory.categoryFor(primaryMuscleGroup)
    val region: MuscleCategory
        get() = category
}
