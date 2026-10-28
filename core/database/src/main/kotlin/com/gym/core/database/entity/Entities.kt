package com.gym.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gym.core.model.Equipment
import com.gym.core.model.MuscleGroup
import com.gym.core.model.PerceivedEffort
import com.gym.core.model.SetType
import com.gym.core.model.WorkoutStatus

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: String, // Comma-separated enums
    val equipment: Equipment,
    val isCustom: Boolean = false,
    val defaultRestSeconds: Int = 90,
    val instructions: String? = null,
    val isExcluded: Boolean = false,
    val exclusionReason: String? = null
)

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null
)

@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("templateId"),
        Index("exerciseId")
    ]
)
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val supersetId: String? = null,
    val targetSetsCount: Int = 3,
    val defaultRestSeconds: Int = 90
)

@Entity(
    tableName = "workouts",
    indices = [
        Index("userId"),
        Index("status")
    ]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "default_user",
    val templateId: Long? = null,
    val title: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationSeconds: Long = 0,
    val status: WorkoutStatus = WorkoutStatus.IN_PROGRESS,
    val notes: String? = null
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("workoutId"),
        Index("exerciseId")
    ]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val supersetId: String? = null,
    val notes: String? = null
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workoutExerciseId")
    ]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val setType: SetType = SetType.NORMAL,
    val weight: Double,
    val reps: Int,
    val rpe: Float? = null,
    val effort: PerceivedEffort? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val restTimeSeconds: Int? = null
)

@Entity(
    tableName = "equipment_settings"
)
data class EquipmentSettingsEntity(
    @PrimaryKey
    val equipment: Equipment,
    val smallWeightThreshold: Double = 10.0,
    val smallWeightStep: Double = 1.0,
    val defaultStep: Double = 2.0,
    val barWeight: Double = 20.0,
    val minPlatesPairWeight: Double = 2.5
)

@Entity(
    tableName = "set_video_reviews",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("setId")
    ]
)
data class SetVideoReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val setId: Long,
    val localVideoUri: String,
    val thumbnailUri: String? = null,
    val durationSeconds: Int = 0,
    val feedbackComment: String? = null,
    val coachRating: Int? = null,
    val coachName: String? = null,
    val reviewedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "coach_assignments")
data class CoachAssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coachId: String,
    val coachName: String,
    val athleteId: String,
    val athleteName: String,
    val templateId: Long,
    val targetRpe: Float? = null,
    val coachNotes: String? = null,
    val assignedDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
