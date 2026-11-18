package com.gym.core.database

import android.content.Context
import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.dao.WorkoutPlanDao
import com.gym.core.database.entity.ExerciseEntity
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.WorkoutPlanDayEntity
import com.gym.core.database.entity.WorkoutPlanEntity
import com.gym.core.database.entity.WorkoutTemplateEntity
import com.gym.core.model.Equipment
import com.gym.core.model.MuscleGroup
import com.gym.core.model.TrainingExperience
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
                val id = obj.optLong("id", (i + 1).toLong())
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
                        id = id,
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

    suspend fun seedDefaultDataIfEmpty(
        context: Context? = null,
        exerciseDao: ExerciseDao,
        templateDao: TemplateDao,
        workoutPlanDao: WorkoutPlanDao? = null
    ) {
        seedMutex.withLock {
            // 1. Проверяем и наполняем/обновляем каталог упражнений (1324 упражнения)
            val catalogExercises = if (context != null) {
                loadCatalogFromAssets(context)
            } else {
                emptyList()
            }

            if (catalogExercises.isNotEmpty()) {
                val existingCount = exerciseDao.getExerciseCount()
                if (existingCount == 0) {
                    exerciseDao.insertExercises(catalogExercises)
                } else {
                    exerciseDao.updateExercises(catalogExercises)
                    exerciseDao.insertExercises(catalogExercises)
                }
            } else if (exerciseDao.getExerciseCount() == 0) {
                exerciseDao.insertExercises(DefaultExercises.list)
            }

            // 2. Проверяем и наполняем 12 мировых шаблонов тренировок
            val currentTemplateCount = templateDao.getTemplateCount()
            if (currentTemplateCount < 12) {
                // Если база пуста или содержит устаревшие шаблоны (< 12) — обновляем базу до полного набора
                templateDao.deleteAllTemplateExercises()
                templateDao.deleteAllTemplates()

                val allExercises = exerciseDao.getAllExercisesList()
                val exercisesById = allExercises.associateBy { it.id }
                val exercisesByName = allExercises.associateBy { it.name.lowercase().trim() }
                val exercisesByNameEn = allExercises.mapNotNull { ex ->
                    ex.nameEn?.lowercase()?.trim()?.let { it to ex }
                }.toMap()

                fun findExercise(nameRu: String, nameEn: String? = null, fallbackId: Long? = null): ExerciseEntity? {
                    if (fallbackId != null && exercisesById.containsKey(fallbackId)) {
                        return exercisesById[fallbackId]
                    }
                    if (nameEn != null && exercisesByNameEn.containsKey(nameEn.lowercase().trim())) {
                        return exercisesByNameEn[nameEn.lowercase().trim()]
                    }
                    val exactRu = exercisesByName[nameRu.lowercase().trim()]
                    if (exactRu != null) return exactRu

                    return allExercises.find { ex ->
                        (nameEn != null && ex.nameEn?.contains(nameEn, ignoreCase = true) == true) ||
                        ex.name.contains(nameRu, ignoreCase = true)
                    }
                }

                data class ExConfig(
                    val nameRu: String,
                    val nameEn: String,
                    val fallbackId: Long,
                    val sets: Int = 3,
                    val rest: Int = 90,
                    val supersetId: String? = null
                )

                suspend fun createTemplate(
                    title: String,
                    description: String,
                    level: TrainingExperience,
                    exercises: List<ExConfig>
                ) {
                    val templateId = templateDao.insertTemplate(
                        WorkoutTemplateEntity(
                            title = title,
                            description = description,
                            experienceLevel = level
                        )
                    )
                    val entities = exercises.mapIndexedNotNull { index, cfg ->
                        val ex = findExercise(cfg.nameRu, cfg.nameEn, cfg.fallbackId)
                        ex?.let {
                            TemplateExerciseEntity(
                                templateId = templateId,
                                exerciseId = it.id,
                                orderIndex = index,
                                supersetId = cfg.supersetId,
                                targetSetsCount = cfg.sets,
                                defaultRestSeconds = cfg.rest
                            )
                        }
                    }
                    if (entities.isNotEmpty()) {
                        templateDao.insertTemplateExercises(entities)
                    }
                }

                // ── НАЧАЛЬНЫЙ УРОВЕНЬ (BEGINNER) ─────────────────────────
                // 1. Фулбоди А: Базовый (Full Body A)
                createTemplate(
                    title = "Фулбоди А: Базовый (Full Body A)",
                    description = "Классический фулбоди по канонам Starting Strength: приседания, жим лежа и тяги для базового силового фундамента",
                    level = TrainingExperience.BEGINNER,
                    exercises = listOf(
                        ExConfig("Приседания со штангой", "Barbell Bench Squat", 100L, sets = 3, rest = 150),
                        ExConfig("Жим штанги лежа", "Barbell Bench Press", 99L, sets = 3, rest = 120),
                        ExConfig("Тяга верхнего блока к груди", "Cable Bar Lateral Pulldown", 273L, sets = 3, rest = 90),
                        ExConfig("Армейский жим стоя", "Barbell Seated Overhead Press", 191L, sets = 3, rest = 90),
                        ExConfig("Подъем штанги на бицепс", "Barbell Curl", 107L, sets = 3, rest = 60),
                        ExConfig("Боковая планка", "Bodyweight Incline Side Plank", 252L, sets = 3, rest = 60)
                    )
                )

                // 2. Фулбоди B: Тяга и Наклон (Full Body B)
                createTemplate(
                    title = "Фулбоди B: Тяга и Наклон (Full Body B)",
                    description = "Вторая фулбоди сессия: становая тяга, наклонный жим гантелей и мощная задняя мышечная цепь",
                    level = TrainingExperience.BEGINNER,
                    exercises = listOf(
                        ExConfig("Становая тяга со штангой", "Barbell Deadlift", 108L, sets = 3, rest = 180),
                        ExConfig("Жим гантелей на наклонной скамье", "Dumbbell Incline Alternate Press", 519L, sets = 3, rest = 90),
                        ExConfig("Тяга штанги в наклоне", "Barbell Bent Over Row", 102L, sets = 3, rest = 120),
                        ExConfig("Жим ногами в тренажере", "Lever Alternate Leg Press", 929L, sets = 3, rest = 90),
                        ExConfig("Разгибания на трицепс на блоке", "Cable Alternate Triceps Extension", 271L, sets = 3, rest = 60),
                        ExConfig("Подъемы на носки со штангой стоя", "Barbell Standing Calf Raise", 210L, sets = 3, rest = 60)
                    )
                )

                // 3. Верх / Низ: Старт (Upper/Lower Intro)
                createTemplate(
                    title = "Верх / Низ: Старт (Upper/Lower Intro)",
                    description = "Двухдневный сплит начального уровня для мягкого перехода от фулбоди к сплит-тренировкам",
                    level = TrainingExperience.BEGINNER,
                    exercises = listOf(
                        ExConfig("Жим штанги лежа", "Barbell Bench Press", 99L, sets = 3, rest = 120),
                        ExConfig("Тяга верхнего блока к груди", "Cable Bar Lateral Pulldown", 273L, sets = 3, rest = 90),
                        ExConfig("Махи гантелями в стороны", "Dumbbell Full Can Lateral Raise", 512L, sets = 3, rest = 60),
                        ExConfig("Сгибания на бицепс 'Молот'", "Dumbbell Alternate Seated Hammer Curl", 467L, sets = 3, rest = 60),
                        ExConfig("Разгибания ног в тренажере", "Lever Leg Extension", 955L, sets = 3, rest = 60),
                        ExConfig("Сгибания ног лежа", "Lever Leg Curl", 952L, sets = 3, rest = 60)
                    )
                )

                // ── СРЕДНИЙ УРОВЕНЬ (INTERMEDIATE) ──────────────────────
                // 4. Push: Грудь, Плечи, Трицепс (PPL)
                createTemplate(
                    title = "Push: Грудь, Плечи, Трицепс (PPL)",
                    description = "Толкающий день классического сплита Push/Pull/Legs: базовые жимы, разводка в кроссовере и трицепс",
                    level = TrainingExperience.INTERMEDIATE,
                    exercises = listOf(
                        ExConfig("Жим штанги лежа", "Barbell Bench Press", 99L, sets = 4, rest = 120),
                        ExConfig("Жим гантелей на наклонной скамье", "Dumbbell Incline Alternate Press", 519L, sets = 3, rest = 90),
                        ExConfig("Сведение рук в кроссовере", "Cable Decline Fly", 283L, sets = 3, rest = 60),
                        ExConfig("Армейский жим стоя", "Barbell Seated Overhead Press", 191L, sets = 3, rest = 90),
                        ExConfig("Махи гантелями в стороны", "Dumbbell Full Can Lateral Raise", 512L, sets = 4, rest = 60),
                        ExConfig("Французский жим лежа", "Barbell Close-Grip Bench Press", 106L, sets = 3, rest = 90),
                        ExConfig("Разгибания на трицепс на блоке", "Cable Alternate Triceps Extension", 271L, sets = 3, rest = 60)
                    )
                )

                // 5. Pull: Спина, Задняя дельта, Бицепс (PPL)
                createTemplate(
                    title = "Pull: Спина, Задняя дельта, Бицепс (PPL)",
                    description = "Тяговый день Push/Pull/Legs: акцент на ширину и толщину широчайших, ромбовидные и двуглавую мышцу",
                    level = TrainingExperience.INTERMEDIATE,
                    exercises = listOf(
                        ExConfig("Подтягивания", "Chin-Up", 437L, sets = 4, rest = 120),
                        ExConfig("Тяга штанги в наклоне", "Barbell Bent Over Row", 102L, sets = 4, rest = 120),
                        ExConfig("Тяга верхнего блока к груди", "Cable Bar Lateral Pulldown", 273L, sets = 3, rest = 90),
                        ExConfig("Пуловер с гантелью", "Dumbbell Straight Arm Pullover", 473L, sets = 3, rest = 60),
                        ExConfig("Подъем штанги на бицепс", "Barbell Curl", 107L, sets = 3, rest = 75),
                        ExConfig("Сгибания на бицепс 'Молот'", "Dumbbell Alternate Seated Hammer Curl", 467L, sets = 3, rest = 60)
                    )
                )

                // 6. Legs: Квадрицепс, Бицепс бедра, Икры (PPL)
                createTemplate(
                    title = "Legs: Квадрицепс, Бицепс бедра, Икры (PPL)",
                    description = "День ног в цикле PPL: тяжелые базовые приседания, румынская тяга и точечная гипертрофия бедер",
                    level = TrainingExperience.INTERMEDIATE,
                    exercises = listOf(
                        ExConfig("Приседания со штангой", "Barbell Bench Squat", 100L, sets = 4, rest = 180),
                        ExConfig("Румынская тяга", "Barbell Romanian Deadlift", 183L, sets = 4, rest = 120),
                        ExConfig("Жим ногами в тренажере", "Lever Alternate Leg Press", 929L, sets = 3, rest = 90),
                        ExConfig("Разгибания ног в тренажере", "Lever Leg Extension", 955L, sets = 3, rest = 60),
                        ExConfig("Сгибания ног лежа", "Lever Leg Curl", 952L, sets = 3, rest = 60),
                        ExConfig("Подъемы на носки со штангой стоя", "Barbell Standing Calf Raise", 210L, sets = 4, rest = 60)
                    )
                )

                // 7. Upper Body: Сила и Объем (PHUL Power)
                createTemplate(
                    title = "Upper Body: Сила и Объем (PHUL Power)",
                    description = "Силовой день верха по протоколу PHUL: тяжелые компаундные движения в силовом диапазоне 4-6 повторов",
                    level = TrainingExperience.INTERMEDIATE,
                    exercises = listOf(
                        ExConfig("Жим штанги лежа", "Barbell Bench Press", 99L, sets = 4, rest = 150),
                        ExConfig("Тяга штанги в наклоне", "Barbell Bent Over Row", 102L, sets = 4, rest = 150),
                        ExConfig("Армейский жим стоя", "Barbell Seated Overhead Press", 191L, sets = 3, rest = 120),
                        ExConfig("Подтягивания", "Chin-Up", 437L, sets = 3, rest = 120),
                        ExConfig("Подъем штанги на бицепс", "Barbell Curl", 107L, sets = 3, rest = 75),
                        ExConfig("Французский жим лежа", "Barbell Close-Grip Bench Press", 106L, sets = 3, rest = 90)
                    )
                )

                // 8. Lower Body: Сила и Мощь (PHUL Power)
                createTemplate(
                    title = "Lower Body: Сила и Мощь (PHUL Power)",
                    description = "Тяжелый день низа по протоколу PHUL: максимальное рекрутирование двигательных единиц в приседе и становой тяге",
                    level = TrainingExperience.INTERMEDIATE,
                    exercises = listOf(
                        ExConfig("Приседания со штангой", "Barbell Bench Squat", 100L, sets = 4, rest = 180),
                        ExConfig("Становая тяга со штангой", "Barbell Deadlift", 108L, sets = 3, rest = 180),
                        ExConfig("Жим ногами в тренажере", "Lever Alternate Leg Press", 929L, sets = 3, rest = 120),
                        ExConfig("Сгибания ног лежа", "Lever Leg Curl", 952L, sets = 3, rest = 75),
                        ExConfig("Подъемы на носки со штангой стоя", "Barbell Standing Calf Raise", 210L, sets = 4, rest = 60)
                    )
                )

                // ── ОПЫТНЫЙ УРОВЕНЬ (ADVANCED) ──────────────────────────
                // 9. Arnold Split: Грудь + Спина (Суперсеты)
                createTemplate(
                    title = "Arnold Split: Грудь + Спина (Суперсеты)",
                    description = "Золотой сплит Арнольда Шварценеггера: объединение антагонистов (грудь и спина) в суперсеты для мощного пампинга",
                    level = TrainingExperience.ADVANCED,
                    exercises = listOf(
                        ExConfig("Жим штанги лежа", "Barbell Bench Press", 99L, sets = 4, rest = 90, supersetId = "arnold_ss1"),
                        ExConfig("Подтягивания", "Chin-Up", 437L, sets = 4, rest = 90, supersetId = "arnold_ss1"),
                        ExConfig("Жим гантелей на наклонной скамье", "Dumbbell Incline Alternate Press", 519L, sets = 3, rest = 90, supersetId = "arnold_ss2"),
                        ExConfig("Тяга штанги в наклоне", "Barbell Bent Over Row", 102L, sets = 3, rest = 90, supersetId = "arnold_ss2"),
                        ExConfig("Пуловер с гантелью", "Dumbbell Straight Arm Pullover", 473L, sets = 3, rest = 60),
                        ExConfig("Сведение рук в кроссовере", "Cable Decline Fly", 283L, sets = 3, rest = 60)
                    )
                )

                // 10. Arnold Split: Плечи + Руки (Суперсеты)
                createTemplate(
                    title = "Arnold Split: Плечи + Руки (Суперсеты)",
                    description = "Второй день Arnold Split: мощная проработка дельт и суперсеты бицепс + трицепс",
                    level = TrainingExperience.ADVANCED,
                    exercises = listOf(
                        ExConfig("Армейский жим стоя", "Barbell Seated Overhead Press", 191L, sets = 4, rest = 120),
                        ExConfig("Махи гантелями в стороны", "Dumbbell Full Can Lateral Raise", 512L, sets = 4, rest = 60),
                        ExConfig("Подъем штанги на бицепс", "Barbell Curl", 107L, sets = 4, rest = 75, supersetId = "arms_ss1"),
                        ExConfig("Французский жим лежа", "Barbell Close-Grip Bench Press", 106L, sets = 4, rest = 75, supersetId = "arms_ss1"),
                        ExConfig("Сгибания на бицепс 'Молот'", "Dumbbell Alternate Seated Hammer Curl", 467L, sets = 3, rest = 60, supersetId = "arms_ss2"),
                        ExConfig("Разгибания на трицепс на блоке", "Cable Alternate Triceps Extension", 271L, sets = 3, rest = 60, supersetId = "arms_ss2")
                    )
                )

                // 11. Renaissance Hypertrophy (RP Dr. Israetel)
                createTemplate(
                    title = "Renaissance Hypertrophy (RP Dr. Israetel)",
                    description = "Научный протокол Renaissance Periodization: максимальный адаптивный объем (MAV), работа в растянутой позиции и RIR 1-2",
                    level = TrainingExperience.ADVANCED,
                    exercises = listOf(
                        ExConfig("Румынская тяга", "Barbell Romanian Deadlift", 183L, sets = 4, rest = 150),
                        ExConfig("Тяга штанги в наклоне", "Barbell Bent Over Row", 102L, sets = 4, rest = 120),
                        ExConfig("Тяга верхнего блока к груди", "Cable Bar Lateral Pulldown", 273L, sets = 4, rest = 90),
                        ExConfig("Сгибания ног лежа", "Lever Leg Curl", 952L, sets = 4, rest = 75),
                        ExConfig("Пуловер с гантелью", "Dumbbell Straight Arm Pullover", 473L, sets = 3, rest = 60),
                        ExConfig("Боковая планка", "Bodyweight Incline Side Plank", 252L, sets = 3, rest = 60)
                    )
                )

                // 12. Powerbuilding 5/3/1: Пиковая Сила
                createTemplate(
                    title = "Powerbuilding 5/3/1: Пиковая Сила",
                    description = "Синтез пауэрлифтинга и бодибилдинга (Jim Wendler 5/3/1): тяжелая тройка и многоповторная гипертрофийная подсобка",
                    level = TrainingExperience.ADVANCED,
                    exercises = listOf(
                        ExConfig("Приседания со штангой", "Barbell Bench Squat", 100L, sets = 5, rest = 180),
                        ExConfig("Жим штанги лежа", "Barbell Bench Press", 99L, sets = 5, rest = 150),
                        ExConfig("Становая тяга со штангой", "Barbell Deadlift", 108L, sets = 4, rest = 180),
                        ExConfig("Отжимания на брусьях", "Chest Dip", 242L, sets = 3, rest = 90),
                        ExConfig("Подтягивания", "Chin-Up", 437L, sets = 3, rest = 90),
                        ExConfig("Подъемы на носки со штангой стоя", "Barbell Standing Calf Raise", 210L, sets = 4, rest = 60)
                    )
                )
            }

            // 3. Проверяем и наполняем недельные тренировочные программы (Workout Plans / Splits)
            if (workoutPlanDao != null && workoutPlanDao.getPlansCount() == 0) {
                seedDefaultWorkoutPlans(templateDao, workoutPlanDao)
            }
        }
    }

    private suspend fun seedDefaultWorkoutPlans(
        templateDao: TemplateDao,
        workoutPlanDao: WorkoutPlanDao
    ) {
        val allTemplates = templateDao.getAllTemplatesList()
        val templateMap = allTemplates.associateBy { it.title.trim() }

        fun findTemplateId(titlePrefix: String): Long? {
            return templateMap.entries.find { it.key.startsWith(titlePrefix, ignoreCase = true) }?.value?.id
        }

        val pplPushId = findTemplateId("Push: Грудь")
        val pplPullId = findTemplateId("Pull: Спина")
        val pplLegsId = findTemplateId("Legs: Квадрицепс")
        val phulUpperId = findTemplateId("Upper Body: Сила")
        val phulLowerId = findTemplateId("Lower Body: Сила")
        val arnoldChestBackId = findTemplateId("Arnold Split: Грудь + Спина")
        val arnoldArmsId = findTemplateId("Arnold Split: Плечи + Руки")
        val fullBodyAId = findTemplateId("Фулбоди А: Базовый")
        val fullBodyBId = findTemplateId("Фулбоди B: Тяга")

        // 1. Push / Pull / Legs (3 дня) - По умолчанию АКТИВНЫЙ
        workoutPlanDao.insertPlanWithDays(
            plan = WorkoutPlanEntity(
                title = "Push / Pull / Legs (PPL)",
                description = "Классический сбалансированный сплит: толчковый день, тяговый день и день ног. Золотой стандарт мышечной гипертрофии.",
                level = TrainingExperience.INTERMEDIATE,
                daysPerWeek = 3,
                isActive = true
            ),
            days = listOf(
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 1, dayNameRu = "Push: Грудь, Плечи, Трицепс", templateId = pplPushId, isRestDay = false, focusDescription = "Базовые жимы, кроссовер и трицепс"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 2, dayNameRu = "День отдыха и восстановления", isRestDay = true, focusDescription = "Суперкомпенсация, сон, полноценное питание"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 3, dayNameRu = "Pull: Спина, Дельты, Бицепс", templateId = pplPullId, isRestDay = false, focusDescription = "Тяги штанги, подтягивания, бицепс"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 4, dayNameRu = "День отдыха и восстановления", isRestDay = true, focusDescription = "Легкая прогулка, восполнение сил"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 5, dayNameRu = "Legs: Квадрицепс, Бедра, Икры", templateId = pplLegsId, isRestDay = false, focusDescription = "Приседания, румынская тяга, жим ногами"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 6, dayNameRu = "День отдыха и восстановления", isRestDay = true, focusDescription = "Релаксация, баня/массаж по желанию"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 7, dayNameRu = "День отдыха и восстановления", isRestDay = true, focusDescription = "План на неделю, настройка экипировки")
            )
        )

        // 2. Верх / Низ (Upper / Lower - PHUL Power 4 дня)
        workoutPlanDao.insertPlanWithDays(
            plan = WorkoutPlanEntity(
                title = "Верх / Низ (PHUL Power 4 дня)",
                description = "Четырехдневный протокол высокой плотности: по 2 тренировки на верх и низ тела в неделю.",
                level = TrainingExperience.INTERMEDIATE,
                daysPerWeek = 4,
                isActive = false
            ),
            days = listOf(
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 1, dayNameRu = "Верх тела: Сила и Объем", templateId = phulUpperId, isRestDay = false, focusDescription = "Тяжелые жимы и тяги"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 2, dayNameRu = "Низ тела: Сила и Мощь", templateId = phulLowerId, isRestDay = false, focusDescription = "Присед и становая тяга"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 3, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Разгрузка ЦНС"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 4, dayNameRu = "Верх тела: Гипертрофия", templateId = phulUpperId, isRestDay = false, focusDescription = "Многоповторная накачка"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 5, dayNameRu = "Низ тела: Рельеф", templateId = phulLowerId, isRestDay = false, focusDescription = "Изоляция бедер и икр"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 6, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Восстановление"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 7, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Подготовка к новому микроциклу")
            )
        )

        // 3. Золотой сплит Арнольда (Arnold Golden Split) 3 дня
        workoutPlanDao.insertPlanWithDays(
            plan = WorkoutPlanEntity(
                title = "Золотой сплит Арнольда (Golden Era)",
                description = "Легендарный сплит Арнольда Шварценеггера: работа антагонистов в суперсетах для экстремального пампинга.",
                level = TrainingExperience.ADVANCED,
                daysPerWeek = 3,
                isActive = false
            ),
            days = listOf(
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 1, dayNameRu = "Грудь + Спина (Суперсеты)", templateId = arnoldChestBackId, isRestDay = false, focusDescription = "Антагонистические суперсеты"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 2, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Восстановление грудных и широчайших"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 3, dayNameRu = "Ноги и икры", templateId = pplLegsId, isRestDay = false, focusDescription = "Тяжелая тренировка квадрицепсов"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 4, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Восстановление"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 5, dayNameRu = "Плечи + Руки (Суперсеты)", templateId = arnoldArmsId, isRestDay = false, focusDescription = "Суперсеты бицепс + трицепс, дельты"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 6, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Глубокий сон и отдых"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 7, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Подготовка к понедельнику")
            )
        )

        // 4. Базовый Фулбоди (Starting Strength) 3 дня
        workoutPlanDao.insertPlanWithDays(
            plan = WorkoutPlanEntity(
                title = "Базовый Фулбоди (Full Body 3 дня)",
                description = "Классический трехдневный фулбоди для новичков: проработка всего тела каждую тренировку через чередование А и B.",
                level = TrainingExperience.BEGINNER,
                daysPerWeek = 3,
                isActive = false
            ),
            days = listOf(
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 1, dayNameRu = "Фулбоди Сессия А", templateId = fullBodyAId, isRestDay = false, focusDescription = "Присед, жим лежа, тяга блока"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 2, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Восстановление мышц всего тела"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 3, dayNameRu = "Фулбоди Сессия B", templateId = fullBodyBId, isRestDay = false, focusDescription = "Становая тяга, наклонный жим, ноги"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 4, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Восстановление"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 5, dayNameRu = "Фулбоди Сессия А", templateId = fullBodyAId, isRestDay = false, focusDescription = "Присед, жим лежа, армейский жим"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 6, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Отдых"),
                WorkoutPlanDayEntity(planId = 0, dayOfWeek = 7, dayNameRu = "День отдыха", isRestDay = true, focusDescription = "Отдых")
            )
        )
    }
}
