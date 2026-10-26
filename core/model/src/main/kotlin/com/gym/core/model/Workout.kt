package com.gym.core.model

data class WorkoutExercise(
    val id: Long = 0,
    val exercise: Exercise,
    val orderIndex: Int,
    val supersetId: String? = null,
    val sets: List<WorkoutSet> = emptyList(),
    val notes: String? = null
) {
    val isPartOfSuperset: Boolean
        get() = !supersetId.isNullOrBlank()

    val totalVolume: Double
        get() = sets.filter { it.isCompleted }.sumOf { it.volume }

    val completedSetsCount: Int
        get() = sets.count { it.isCompleted }
}

data class Workout(
    val id: Long = 0,
    val templateId: Long? = null,
    val title: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationSeconds: Long = 0,
    val exercises: List<WorkoutExercise> = emptyList(),
    val status: WorkoutStatus = WorkoutStatus.IN_PROGRESS,
    val notes: String? = null
) {
    val totalVolume: Double
        get() = exercises.sumOf { it.totalVolume }

    val totalCompletedSets: Int
        get() = exercises.sumOf { it.completedSetsCount }
}
