package com.gym.core.database

import com.gym.core.database.backup.BackupManager
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.dao.TemplateExerciseWithDetails
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.database.dao.WorkoutDao
import com.gym.core.database.dao.WorkoutExerciseWithSets
import com.gym.core.database.dao.WorkoutTemplateWithExercises
import com.gym.core.database.dao.WorkoutWithDetails
import com.gym.core.database.entity.ExerciseEntity
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.UserProfileEntity
import com.gym.core.database.entity.WorkoutEntity
import com.gym.core.database.entity.WorkoutExerciseEntity
import com.gym.core.database.entity.WorkoutSetEntity
import com.gym.core.database.entity.WorkoutTemplateEntity
import com.gym.core.model.Equipment
import com.gym.core.model.FitnessGoal
import com.gym.core.model.Gender
import com.gym.core.model.MuscleGroup
import com.gym.core.model.SetType
import com.gym.core.model.TrainingExperience
import com.gym.core.model.WorkoutStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class BackupManagerTest {

    private val sampleProfile = UserProfileEntity(
        id = "default_user",
        name = "Тестовый Атлет",
        gender = Gender.MALE,
        age = 30,
        heightCm = 182f,
        weightKg = 84f,
        experience = TrainingExperience.INTERMEDIATE,
        primaryGoal = FitnessGoal.HYPERTROPHY,
        workoutsPerWeek = 4,
        isCompleted = true
    )

    private val sampleExercise = ExerciseEntity(
        id = 99L,
        name = "Жим штанги лежа",
        nameEn = "Barbell Bench Press",
        primaryMuscleGroup = MuscleGroup.CHEST,
        secondaryMuscleGroups = "TRICEPS,SHOULDERS",
        equipment = Equipment.BARBELL,
        isCustom = false,
        defaultRestSeconds = 120,
        instructions = "Опустите штангу на грудь и выжмите вверх",
        gifUrl = null,
        thumbnailUrl = null,
        isExcluded = false,
        exclusionReason = null
    )

    private val sampleWorkoutWithDetails = WorkoutWithDetails(
        workout = WorkoutEntity(
            id = 1L,
            userId = "default_user",
            templateId = null,
            title = "Тестовая тренировка груди",
            startTime = 1700000000000L,
            endTime = 1700003600000L,
            durationSeconds = 3600L,
            status = WorkoutStatus.COMPLETED,
            notes = "Отличная сессия"
        ),
        exercises = listOf(
            WorkoutExerciseWithSets(
                workoutExercise = WorkoutExerciseEntity(
                    id = 10L,
                    workoutId = 1L,
                    exerciseId = 99L,
                    orderIndex = 0
                ),
                exercise = sampleExercise,
                sets = listOf(
                    WorkoutSetEntity(
                        id = 100L,
                        workoutExerciseId = 10L,
                        setNumber = 1,
                        setType = SetType.NORMAL,
                        weight = 100.0,
                        reps = 10,
                        rpe = 8.5f,
                        effort = null,
                        isCompleted = true,
                        completedAt = 1700000600000L
                    )
                )
            )
        )
    )

    private val sampleTemplateWithExercises = WorkoutTemplateWithExercises(
        template = WorkoutTemplateEntity(
            id = 1L,
            title = "Push Day",
            description = "Жимовая тренировка",
            experienceLevel = TrainingExperience.INTERMEDIATE,
            createdAt = 1700000000000L
        ),
        exercises = listOf(
            TemplateExerciseWithDetails(
                templateExercise = TemplateExerciseEntity(
                    id = 5L,
                    templateId = 1L,
                    exerciseId = 99L,
                    orderIndex = 0,
                    targetSetsCount = 4,
                    defaultRestSeconds = 120
                ),
                exercise = sampleExercise
            )
        )
    )

    @Test
    fun testExportBackupJsonStructure() = runTest {
        val fakeWorkoutDao = createFakeDao<WorkoutDao> { method, _ ->
            when (method.name) {
                "getCompletedWorkouts" -> flowOf(listOf(sampleWorkoutWithDetails))
                else -> null
            }
        }

        val fakeTemplateDao = createFakeDao<TemplateDao> { method, _ ->
            when (method.name) {
                "getAllTemplatesWithExercises" -> flowOf(listOf(sampleTemplateWithExercises))
                else -> null
            }
        }

        val fakeUserProfileDao = createFakeDao<UserProfileDao> { method, _ ->
            when (method.name) {
                "getUserProfileSync" -> sampleProfile
                else -> null
            }
        }

        val backupManager = BackupManager(
            database = null,
            workoutDao = fakeWorkoutDao,
            templateDao = fakeTemplateDao,
            userProfileDao = fakeUserProfileDao
        )

        val jsonString = backupManager.exportBackupJson()
        val root = JSONObject(jsonString)

        assertEquals("The Iron Apex", root.getString("appName"))
        assertEquals(1, root.getInt("version"))
        assertTrue(root.has("exportedAt"))

        // Проверка профиля
        val profileJson = root.getJSONObject("profile")
        assertEquals("Тестовый Атлет", profileJson.getString("name"))
        assertEquals(30, profileJson.getInt("age"))

        // Проверка шаблонов
        val templatesJson = root.getJSONArray("templates")
        assertEquals(1, templatesJson.length())
        assertEquals("Push Day", templatesJson.getJSONObject(0).getString("title"))

        // Проверка тренировок
        val workoutsJson = root.getJSONArray("workouts")
        assertEquals(1, workoutsJson.length())
        assertEquals("Тестовая тренировка груди", workoutsJson.getJSONObject(0).getString("title"))
    }

    @Test
    fun testExportWorkoutsCsvFormat() = runTest {
        val fakeWorkoutDao = createFakeDao<WorkoutDao> { method, _ ->
            when (method.name) {
                "getCompletedWorkouts" -> flowOf(listOf(sampleWorkoutWithDetails))
                else -> null
            }
        }
        val fakeTemplateDao = createFakeDao<TemplateDao> { _, _ -> null }
        val fakeUserProfileDao = createFakeDao<UserProfileDao> { _, _ -> null }
        val backupManager = BackupManager(
            database = null,
            workoutDao = fakeWorkoutDao,
            templateDao = fakeTemplateDao,
            userProfileDao = fakeUserProfileDao
        )

        val csv = backupManager.exportWorkoutsCsv()
        val lines = csv.trim().split("\n")

        // Проверка заголовка
        assertEquals("Date,Workout Title,Exercise Name,Set,Weight (kg),Reps,Set Type,RPE,Completed", lines[0])

        // Проверка строки данных
        assertTrue(lines.size >= 2)
        val dataLine = lines[1]
        assertTrue(dataLine.contains("Тестовая тренировка груди"))
        assertTrue(dataLine.contains("Жим штанги лежа"))
        assertTrue(dataLine.contains("100.0,10"))
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> createFakeDao(crossinline handler: (java.lang.reflect.Method, Array<out Any?>?) -> Any?): T {
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java)
        ) { _, method, args ->
            handler(method, args)
        } as T
    }
}
