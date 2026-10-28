package com.gym.core.database

import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.WorkoutTemplateEntity

object DefaultTemplates {

    suspend fun seedDefaultDataIfEmpty(exerciseDao: ExerciseDao, templateDao: TemplateDao) {
        // 1. Проверяем и наполняем упражнения
        if (exerciseDao.getExerciseCount() == 0) {
            exerciseDao.insertExercises(DefaultExercises.list)
        }

        // 2. Проверяем и наполняем стартовые шаблоны
        if (templateDao.getTemplateCount() == 0) {
            val exercisesMap = exerciseDao.getAllExercisesList().associateBy { it.name }

            // Шаблон 1: Грудь + Трицепс (Push)
            val pushTemplateId = templateDao.insertTemplate(
                WorkoutTemplateEntity(
                    title = "Грудь + Трицепс (Push)",
                    description = "Акцент на грудные мышцы и трицепс, базовые жимы и изоляция"
                )
            )
            val pushExercises = listOfNotNull(
                exercisesMap["Жим штанги лежа"]?.let {
                    TemplateExerciseEntity(templateId = pushTemplateId, exerciseId = it.id, orderIndex = 0, targetSetsCount = 4, defaultRestSeconds = 120)
                },
                exercisesMap["Жим гантелей на наклонной скамье"]?.let {
                    TemplateExerciseEntity(templateId = pushTemplateId, exerciseId = it.id, orderIndex = 1, targetSetsCount = 3, defaultRestSeconds = 90)
                },
                exercisesMap["Сведение рук в кроссовере"]?.let {
                    TemplateExerciseEntity(templateId = pushTemplateId, exerciseId = it.id, orderIndex = 2, targetSetsCount = 3, defaultRestSeconds = 60)
                },
                exercisesMap["Французский жим лежа"]?.let {
                    TemplateExerciseEntity(templateId = pushTemplateId, exerciseId = it.id, orderIndex = 3, targetSetsCount = 3, defaultRestSeconds = 90)
                },
                exercisesMap["Разгибание на трицепс на верхнем блоке"]?.let {
                    TemplateExerciseEntity(templateId = pushTemplateId, exerciseId = it.id, orderIndex = 4, targetSetsCount = 3, defaultRestSeconds = 60)
                }
            )
            templateDao.insertTemplateExercises(pushExercises)

            // Шаблон 2: Спина + Бицепс (Pull)
            val pullTemplateId = templateDao.insertTemplate(
                WorkoutTemplateEntity(
                    title = "Спина + Бицепс (Pull)",
                    description = "Тяговая тренировка: широчайшие, трапеции и бицепс"
                )
            )
            val pullExercises = listOfNotNull(
                exercisesMap["Подтягивания широким хватом"]?.let {
                    TemplateExerciseEntity(templateId = pullTemplateId, exerciseId = it.id, orderIndex = 0, targetSetsCount = 4, defaultRestSeconds = 120)
                },
                exercisesMap["Тяга штанги в наклоне"]?.let {
                    TemplateExerciseEntity(templateId = pullTemplateId, exerciseId = it.id, orderIndex = 1, targetSetsCount = 3, defaultRestSeconds = 120)
                },
                exercisesMap["Тяга верхнего блока к груди"]?.let {
                    TemplateExerciseEntity(templateId = pullTemplateId, exerciseId = it.id, orderIndex = 2, targetSetsCount = 3, defaultRestSeconds = 90)
                },
                exercisesMap["Подъем штанги на бицепс"]?.let {
                    TemplateExerciseEntity(templateId = pullTemplateId, exerciseId = it.id, orderIndex = 3, targetSetsCount = 3, defaultRestSeconds = 75)
                },
                exercisesMap["Сгибания на бицепс с гантелями 'Молот'"]?.let {
                    TemplateExerciseEntity(templateId = pullTemplateId, exerciseId = it.id, orderIndex = 4, targetSetsCount = 3, defaultRestSeconds = 60)
                }
            )
            templateDao.insertTemplateExercises(pullExercises)

            // Шаблон 3: Ноги + Плечи (Legs & Shoulders)
            val legsTemplateId = templateDao.insertTemplate(
                WorkoutTemplateEntity(
                    title = "Ноги + Плечи (Legs & Shoulders)",
                    description = "Базовый день ног и проработка всех пучков дельт"
                )
            )
            val legsExercises = listOfNotNull(
                exercisesMap["Приседания со штангой на плечах"]?.let {
                    TemplateExerciseEntity(templateId = legsTemplateId, exerciseId = it.id, orderIndex = 0, targetSetsCount = 4, defaultRestSeconds = 180)
                },
                exercisesMap["Жим ногами в тренажере"]?.let {
                    TemplateExerciseEntity(templateId = legsTemplateId, exerciseId = it.id, orderIndex = 1, targetSetsCount = 3, defaultRestSeconds = 120)
                },
                exercisesMap["Армейский жим стоя (Overhead Press)"]?.let {
                    TemplateExerciseEntity(templateId = legsTemplateId, exerciseId = it.id, orderIndex = 2, targetSetsCount = 3, defaultRestSeconds = 120)
                },
                exercisesMap["Махи гантелями в стороны"]?.let {
                    TemplateExerciseEntity(templateId = legsTemplateId, exerciseId = it.id, orderIndex = 3, targetSetsCount = 3, defaultRestSeconds = 60)
                },
                exercisesMap["Подъемы на носки стоя (икры)"]?.let {
                    TemplateExerciseEntity(templateId = legsTemplateId, exerciseId = it.id, orderIndex = 4, targetSetsCount = 3, defaultRestSeconds = 60)
                }
            )
            templateDao.insertTemplateExercises(legsExercises)

            // Шаблон 4: Фулбоди (Full Body)
            val fullBodyTemplateId = templateDao.insertTemplate(
                WorkoutTemplateEntity(
                    title = "Фулбоди (Full Body)",
                    description = "Комплексная проработка всех основных мышечных групп за одну сессию"
                )
            )
            val fullBodyExercises = listOfNotNull(
                exercisesMap["Приседания со штангой на плечах"]?.let {
                    TemplateExerciseEntity(templateId = fullBodyTemplateId, exerciseId = it.id, orderIndex = 0, targetSetsCount = 3, defaultRestSeconds = 180)
                },
                exercisesMap["Жим штанги лежа"]?.let {
                    TemplateExerciseEntity(templateId = fullBodyTemplateId, exerciseId = it.id, orderIndex = 1, targetSetsCount = 3, defaultRestSeconds = 120)
                },
                exercisesMap["Подтягивания широким хватом"]?.let {
                    TemplateExerciseEntity(templateId = fullBodyTemplateId, exerciseId = it.id, orderIndex = 2, targetSetsCount = 3, defaultRestSeconds = 120)
                },
                exercisesMap["Армейский жим стоя (Overhead Press)"]?.let {
                    TemplateExerciseEntity(templateId = fullBodyTemplateId, exerciseId = it.id, orderIndex = 3, targetSetsCount = 3, defaultRestSeconds = 90)
                },
                exercisesMap["Подъем штанги на бицепс"]?.let {
                    TemplateExerciseEntity(templateId = fullBodyTemplateId, exerciseId = it.id, orderIndex = 4, targetSetsCount = 3, defaultRestSeconds = 75)
                },
                exercisesMap["Планка"]?.let {
                    TemplateExerciseEntity(templateId = fullBodyTemplateId, exerciseId = it.id, orderIndex = 5, targetSetsCount = 3, defaultRestSeconds = 60)
                }
            )
            templateDao.insertTemplateExercises(fullBodyExercises)
        }
    }
}
