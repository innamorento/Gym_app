package com.gym.core.database

import android.content.Context
import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.entity.ExerciseEntity
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.WorkoutTemplateEntity
import com.gym.core.model.Equipment
import com.gym.core.model.MuscleGroup
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray

object DefaultTemplates {

    private val seedMutex = Mutex()

    private fun loadCatalogFromAssets(context: Context): List<ExerciseEntity> {
        val list = mutableListOf<ExerciseEntity>()
        try {
            val jsonString = context.assets.open("exercises_catalog.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.getString("name")
                val nameEn = obj.optString("nameEn").takeIf { it.isNotBlank() }
                val primaryMuscleGroup = try {
                    MuscleGroup.valueOf(obj.getString("primaryMuscleGroup"))
                } catch (e: Exception) {
                    MuscleGroup.FULL_BODY
                }
                val secondaryMuscleGroups = obj.optString("secondaryMuscleGroups", "")
                val equipment = try {
                    Equipment.valueOf(obj.getString("equipment"))
                } catch (e: Exception) {
                    Equipment.OTHER
                }
                val defaultRestSeconds = obj.optInt("defaultRestSeconds", 90)
                val instructions = obj.optString("instructions").takeIf { it.isNotBlank() }
                val gifUrl = obj.optString("gifUrl").takeIf { it.isNotBlank() }
                val thumbnailUrl = obj.optString("thumbnailUrl").takeIf { it.isNotBlank() }

                list.add(
                    ExerciseEntity(
                        id = 0,
                        name = name,
                        nameEn = nameEn,
                        primaryMuscleGroup = primaryMuscleGroup,
                        secondaryMuscleGroups = secondaryMuscleGroups,
                        equipment = equipment,
                        isCustom = false,
                        defaultRestSeconds = defaultRestSeconds,
                        instructions = instructions,
                        gifUrl = gifUrl,
                        thumbnailUrl = thumbnailUrl,
                        isExcluded = false,
                        exclusionReason = null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    suspend fun seedDefaultDataIfEmpty(context: Context? = null, exerciseDao: ExerciseDao, templateDao: TemplateDao) {
        seedMutex.withLock {
            // 1. Проверяем и наполняем упражнения
            val existingNames = exerciseDao.getAllExercisesList().map { it.name }.toSet()
            val catalogExercises = if (context != null) {
                loadCatalogFromAssets(context)
            } else {
                emptyList()
            }

            val exercisesToInsert = if (catalogExercises.isNotEmpty()) {
                catalogExercises.filter { it.name !in existingNames }
            } else if (existingNames.isEmpty()) {
                DefaultExercises.list
            } else {
                emptyList()
            }

            if (exercisesToInsert.isNotEmpty()) {
                exerciseDao.insertExercises(exercisesToInsert)
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
}
