package com.gym.core.database

import com.gym.core.database.entity.ExerciseEntity
import com.gym.core.model.Equipment
import com.gym.core.model.MuscleGroup

object DefaultExercises {
    val list: List<ExerciseEntity> = listOf(
        // Грудь
        ExerciseEntity(
            name = "Жим штанги лежа",
            primaryMuscleGroup = MuscleGroup.CHEST,
            secondaryMuscleGroups = "TRICEPS,SHOULDERS",
            equipment = Equipment.BARBELL,
            defaultRestSeconds = 120
        ),
        ExerciseEntity(
            name = "Жим гантелей на наклонной скамье",
            primaryMuscleGroup = MuscleGroup.CHEST,
            secondaryMuscleGroups = "TRICEPS,SHOULDERS",
            equipment = Equipment.DUMBBELL,
            defaultRestSeconds = 90
        ),
        ExerciseEntity(
            name = "Отжимания на брусьях (грудной акцент)",
            primaryMuscleGroup = MuscleGroup.CHEST,
            secondaryMuscleGroups = "TRICEPS,SHOULDERS",
            equipment = Equipment.BODYWEIGHT,
            defaultRestSeconds = 90
        ),
        ExerciseEntity(
            name = "Сведение рук в кроссовере",
            primaryMuscleGroup = MuscleGroup.CHEST,
            secondaryMuscleGroups = "",
            equipment = Equipment.CABLE,
            defaultRestSeconds = 60
        ),

        // Спина
        ExerciseEntity(
            name = "Становая тяга",
            primaryMuscleGroup = MuscleGroup.BACK,
            secondaryMuscleGroups = "LEGS_HAMSTRINGS,CORE",
            equipment = Equipment.BARBELL,
            defaultRestSeconds = 180
        ),
        ExerciseEntity(
            name = "Подтягивания широким хватом",
            primaryMuscleGroup = MuscleGroup.BACK,
            secondaryMuscleGroups = "BICEPS",
            equipment = Equipment.BODYWEIGHT,
            defaultRestSeconds = 120
        ),
        ExerciseEntity(
            name = "Тяга штанги в наклоне",
            primaryMuscleGroup = MuscleGroup.BACK,
            secondaryMuscleGroups = "BICEPS,CORE",
            equipment = Equipment.BARBELL,
            defaultRestSeconds = 120
        ),
        ExerciseEntity(
            name = "Тяга верхнего блока к груди",
            primaryMuscleGroup = MuscleGroup.BACK,
            secondaryMuscleGroups = "BICEPS",
            equipment = Equipment.CABLE,
            defaultRestSeconds = 90
        ),
        ExerciseEntity(
            name = "Тяга горизонтального блока к поясу",
            primaryMuscleGroup = MuscleGroup.BACK,
            secondaryMuscleGroups = "BICEPS",
            equipment = Equipment.CABLE,
            defaultRestSeconds = 90
        ),

        // Ноги
        ExerciseEntity(
            name = "Приседания со штангой на плечах",
            primaryMuscleGroup = MuscleGroup.LEGS_QUADS,
            secondaryMuscleGroups = "CORE,LEGS_HAMSTRINGS",
            equipment = Equipment.BARBELL,
            defaultRestSeconds = 180
        ),
        ExerciseEntity(
            name = "Жим ногами в тренажере",
            primaryMuscleGroup = MuscleGroup.LEGS_QUADS,
            secondaryMuscleGroups = "LEGS_HAMSTRINGS",
            equipment = Equipment.MACHINE,
            defaultRestSeconds = 120
        ),
        ExerciseEntity(
            name = "Выпады с гантелями",
            primaryMuscleGroup = MuscleGroup.LEGS_QUADS,
            secondaryMuscleGroups = "LEGS_HAMSTRINGS",
            equipment = Equipment.DUMBBELL,
            defaultRestSeconds = 90
        ),
        ExerciseEntity(
            name = "Сгибания ног лежа в тренажере",
            primaryMuscleGroup = MuscleGroup.LEGS_HAMSTRINGS,
            secondaryMuscleGroups = "CALVES",
            equipment = Equipment.MACHINE,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Подъемы на носки стоя (икры)",
            primaryMuscleGroup = MuscleGroup.CALVES,
            secondaryMuscleGroups = "",
            equipment = Equipment.MACHINE,
            defaultRestSeconds = 60
        ),

        // Плечи
        ExerciseEntity(
            name = "Армейский жим стоя (Overhead Press)",
            primaryMuscleGroup = MuscleGroup.SHOULDERS,
            secondaryMuscleGroups = "TRICEPS,CORE",
            equipment = Equipment.BARBELL,
            defaultRestSeconds = 120
        ),
        ExerciseEntity(
            name = "Жим гантелей сидя",
            primaryMuscleGroup = MuscleGroup.SHOULDERS,
            secondaryMuscleGroups = "TRICEPS",
            equipment = Equipment.DUMBBELL,
            defaultRestSeconds = 90
        ),
        ExerciseEntity(
            name = "Махи гантелями в стороны",
            primaryMuscleGroup = MuscleGroup.SHOULDERS,
            secondaryMuscleGroups = "",
            equipment = Equipment.DUMBBELL,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Махи гантелями в наклоне (задняя дельта)",
            primaryMuscleGroup = MuscleGroup.SHOULDERS,
            secondaryMuscleGroups = "BACK",
            equipment = Equipment.DUMBBELL,
            defaultRestSeconds = 60
        ),

        // Руки
        ExerciseEntity(
            name = "Подъем штанги на бицепс",
            primaryMuscleGroup = MuscleGroup.BICEPS,
            secondaryMuscleGroups = "",
            equipment = Equipment.BARBELL,
            defaultRestSeconds = 75
        ),
        ExerciseEntity(
            name = "Сгибания на бицепс с гантелями 'Молот'",
            primaryMuscleGroup = MuscleGroup.BICEPS,
            secondaryMuscleGroups = "",
            equipment = Equipment.DUMBBELL,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Сгибания на скамье Скотта",
            primaryMuscleGroup = MuscleGroup.BICEPS,
            secondaryMuscleGroups = "",
            equipment = Equipment.BARBELL,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Французский жим лежа",
            primaryMuscleGroup = MuscleGroup.TRICEPS,
            secondaryMuscleGroups = "",
            equipment = Equipment.BARBELL,
            defaultRestSeconds = 90
        ),
        ExerciseEntity(
            name = "Разгибание на трицепс на верхнем блоке",
            primaryMuscleGroup = MuscleGroup.TRICEPS,
            secondaryMuscleGroups = "",
            equipment = Equipment.CABLE,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Отжимания от скамьи на трицепс",
            primaryMuscleGroup = MuscleGroup.TRICEPS,
            secondaryMuscleGroups = "CHEST",
            equipment = Equipment.BODYWEIGHT,
            defaultRestSeconds = 60
        ),

        // Кор / Пресс
        ExerciseEntity(
            name = "Скручивания на наклонной скамье",
            primaryMuscleGroup = MuscleGroup.CORE,
            secondaryMuscleGroups = "",
            equipment = Equipment.BODYWEIGHT,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Планка",
            primaryMuscleGroup = MuscleGroup.CORE,
            secondaryMuscleGroups = "FULL_BODY",
            equipment = Equipment.BODYWEIGHT,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Подъем ног в висе на турнике",
            primaryMuscleGroup = MuscleGroup.CORE,
            secondaryMuscleGroups = "",
            equipment = Equipment.BODYWEIGHT,
            defaultRestSeconds = 60
        ),

        // Прочее / Все тело
        ExerciseEntity(
            name = "Гиперэкстензия",
            primaryMuscleGroup = MuscleGroup.FULL_BODY,
            secondaryMuscleGroups = "BACK,LEGS_HAMSTRINGS",
            equipment = Equipment.BODYWEIGHT,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Бёрпи / Разминка",
            primaryMuscleGroup = MuscleGroup.FULL_BODY,
            secondaryMuscleGroups = "CORE,LEGS_QUADS",
            equipment = Equipment.BODYWEIGHT,
            defaultRestSeconds = 60
        ),
        ExerciseEntity(
            name = "Махи гирей (Kettlebell Swing)",
            primaryMuscleGroup = MuscleGroup.FULL_BODY,
            secondaryMuscleGroups = "LEGS_HAMSTRINGS,CORE,SHOULDERS",
            equipment = Equipment.KETTLEBELL,
            defaultRestSeconds = 60
        )
    )
}
