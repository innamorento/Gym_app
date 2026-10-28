package com.gym.core.database.mapper

import com.gym.core.database.dao.TemplateExerciseWithDetails
import com.gym.core.database.dao.WorkoutExerciseWithSets
import com.gym.core.database.dao.WorkoutTemplateWithExercises
import com.gym.core.database.dao.WorkoutWithDetails
import com.gym.core.database.entity.ExerciseEntity
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.WorkoutEntity
import com.gym.core.database.entity.WorkoutExerciseEntity
import com.gym.core.database.entity.WorkoutSetEntity
import com.gym.core.database.entity.WorkoutTemplateEntity
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleGroup
import com.gym.core.model.TemplateExercise
import com.gym.core.model.Workout
import com.gym.core.model.WorkoutExercise
import com.gym.core.model.WorkoutSet
import com.gym.core.model.WorkoutTemplate

fun ExerciseEntity.toDomain(): Exercise {
    val secondary = if (secondaryMuscleGroups.isBlank()) {
        emptyList()
    } else {
        secondaryMuscleGroups.split(",").mapNotNull { name ->
            try {
                MuscleGroup.valueOf(name.trim())
            } catch (e: Exception) {
                null
            }
        }
    }
    return Exercise(
        id = id,
        name = name,
        primaryMuscleGroup = primaryMuscleGroup,
        secondaryMuscleGroups = secondary,
        equipment = equipment,
        isCustom = isCustom,
        defaultRestSeconds = defaultRestSeconds,
        instructions = instructions,
        isExcluded = isExcluded,
        exclusionReason = exclusionReason
    )
}

fun Exercise.toEntity(): ExerciseEntity {
    return ExerciseEntity(
        id = id,
        name = name,
        primaryMuscleGroup = primaryMuscleGroup,
        secondaryMuscleGroups = secondaryMuscleGroups.joinToString(",") { it.name },
        equipment = equipment,
        isCustom = isCustom,
        defaultRestSeconds = defaultRestSeconds,
        instructions = instructions,
        isExcluded = isExcluded,
        exclusionReason = exclusionReason
    )
}

fun TemplateExerciseWithDetails.toDomain(): TemplateExercise {
    return TemplateExercise(
        id = templateExercise.id,
        exercise = exercise.toDomain(),
        orderIndex = templateExercise.orderIndex,
        supersetId = templateExercise.supersetId,
        targetSetsCount = templateExercise.targetSetsCount,
        defaultRestSeconds = templateExercise.defaultRestSeconds
    )
}

fun WorkoutTemplateWithExercises.toDomain(): WorkoutTemplate {
    return WorkoutTemplate(
        id = template.id,
        title = template.title,
        description = template.description,
        exercises = exercises.map { it.toDomain() }.sortedBy { it.orderIndex },
        createdAt = template.createdAt,
        lastUsedAt = template.lastUsedAt
    )
}

fun WorkoutSetEntity.toDomain(): WorkoutSet {
    return WorkoutSet(
        id = id,
        setNumber = setNumber,
        setType = setType,
        weight = weight,
        reps = reps,
        rpe = rpe,
        effort = effort,
        isCompleted = isCompleted,
        completedAt = completedAt,
        restTimeSeconds = restTimeSeconds
    )
}

fun WorkoutSet.toEntity(workoutExerciseId: Long): WorkoutSetEntity {
    return WorkoutSetEntity(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        setType = setType,
        weight = weight,
        reps = reps,
        rpe = rpe,
        effort = effort,
        isCompleted = isCompleted,
        completedAt = completedAt,
        restTimeSeconds = restTimeSeconds
    )
}

fun WorkoutExerciseWithSets.toDomain(): WorkoutExercise {
    return WorkoutExercise(
        id = workoutExercise.id,
        exercise = exercise.toDomain(),
        orderIndex = workoutExercise.orderIndex,
        supersetId = workoutExercise.supersetId,
        sets = sets.map { it.toDomain() }.sortedBy { it.setNumber },
        notes = workoutExercise.notes
    )
}

fun WorkoutWithDetails.toDomain(): Workout {
    return Workout(
        id = workout.id,
        userId = workout.userId,
        templateId = workout.templateId,
        title = workout.title,
        startTime = workout.startTime,
        endTime = workout.endTime,
        durationSeconds = workout.durationSeconds,
        exercises = exercises.map { it.toDomain() }.sortedBy { it.orderIndex },
        status = workout.status,
        notes = workout.notes
    )
}
