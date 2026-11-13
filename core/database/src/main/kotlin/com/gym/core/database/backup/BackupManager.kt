package com.gym.core.database.backup

import com.gym.core.database.GymDatabase
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.database.dao.WorkoutDao
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.UserProfileEntity
import com.gym.core.database.entity.WorkoutEntity
import com.gym.core.database.entity.WorkoutExerciseEntity
import com.gym.core.database.entity.WorkoutSetEntity
import com.gym.core.database.entity.WorkoutTemplateEntity
import com.gym.core.model.FitnessGoal
import com.gym.core.model.Gender
import com.gym.core.model.PerceivedEffort
import com.gym.core.model.SetType
import com.gym.core.model.TrainingExperience
import com.gym.core.model.WorkoutStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ImportSummary(
    val templatesRestored: Int,
    val workoutsRestored: Int,
    val profileRestored: Boolean
)

class BackupManager(
    private val database: GymDatabase? = null,
    private val workoutDao: WorkoutDao,
    private val templateDao: TemplateDao,
    private val userProfileDao: UserProfileDao
) {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    private val csvDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    /**
     * Экспорт всей базы в единый JSON файл бэкапа
     */
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "The Iron Apex")
        root.put("exportedAt", isoDateFormat.format(Date()))

        // 1. Профиль
        val profile = userProfileDao.getUserProfileSync()
        if (profile != null) {
            val profObj = JSONObject()
            profObj.put("id", profile.id)
            profObj.put("name", profile.name)
            profObj.put("gender", profile.gender.name)
            profObj.put("age", profile.age)
            profObj.put("heightCm", profile.heightCm.toDouble())
            profObj.put("weightKg", profile.weightKg.toDouble())
            profObj.put("experience", profile.experience.name)
            profObj.put("primaryGoal", profile.primaryGoal.name)
            profObj.put("workoutsPerWeek", profile.workoutsPerWeek)
            profObj.put("isCompleted", profile.isCompleted)
            profObj.put("role", profile.role.name)
            profObj.put("email", profile.email.orEmpty())
            profObj.put("isGuest", profile.isGuest)
            profObj.put("coachInviteCode", profile.coachInviteCode.orEmpty())
            profObj.put("linkedCoachCode", profile.linkedCoachCode.orEmpty())
            root.put("profile", profObj)
        }

        // 2. Шаблоны тренировок
        val templatesWithExercises = templateDao.getAllTemplatesWithExercises().first()
        val templatesArray = JSONArray()
        for (item in templatesWithExercises) {
            val tObj = JSONObject()
            tObj.put("title", item.template.title)
            tObj.put("description", item.template.description.orEmpty())
            tObj.put("experienceLevel", item.template.experienceLevel.name)
            tObj.put("createdAt", item.template.createdAt)

            val exArray = JSONArray()
            for (exItem in item.exercises) {
                val eObj = JSONObject()
                eObj.put("exerciseId", exItem.templateExercise.exerciseId)
                eObj.put("orderIndex", exItem.templateExercise.orderIndex)
                eObj.put("supersetId", exItem.templateExercise.supersetId.orEmpty())
                eObj.put("targetSetsCount", exItem.templateExercise.targetSetsCount)
                eObj.put("defaultRestSeconds", exItem.templateExercise.defaultRestSeconds)
                exArray.put(eObj)
            }
            tObj.put("exercises", exArray)
            templatesArray.put(tObj)
        }
        root.put("templates", templatesArray)

        // 3. Завершенные тренировки
        val completedWorkouts = workoutDao.getCompletedWorkouts().first()
        val workoutsArray = JSONArray()
        for (w in completedWorkouts) {
            val wObj = JSONObject()
            wObj.put("title", w.workout.title)
            wObj.put("startTime", w.workout.startTime)
            wObj.put("endTime", w.workout.endTime ?: w.workout.startTime)
            wObj.put("durationSeconds", w.workout.durationSeconds)
            wObj.put("notes", w.workout.notes.orEmpty())

            val exListArray = JSONArray()
            for (we in w.exercises) {
                val weObj = JSONObject()
                weObj.put("exerciseId", we.workoutExercise.exerciseId)
                weObj.put("orderIndex", we.workoutExercise.orderIndex)
                weObj.put("supersetId", we.workoutExercise.supersetId.orEmpty())

                val setsArray = JSONArray()
                for (s in we.sets) {
                    val sObj = JSONObject()
                    sObj.put("setNumber", s.setNumber)
                    sObj.put("weight", s.weight)
                    sObj.put("reps", s.reps)
                    sObj.put("setType", s.setType.name)
                    sObj.put("rpe", s.rpe?.toDouble() ?: 0.0)
                    sObj.put("effort", s.effort?.name.orEmpty())
                    sObj.put("isCompleted", s.isCompleted)
                    sObj.put("completedAt", s.completedAt ?: 0L)
                    setsArray.put(sObj)
                }
                weObj.put("sets", setsArray)
                exListArray.put(weObj)
            }
            wObj.put("exercises", exListArray)
            workoutsArray.put(wObj)
        }
        root.put("workouts", workoutsArray)

        root.toString(2)
    }

    /**
     * Экспорт тренировок в CSV для Excel и Google Sheets
     */
    suspend fun exportWorkoutsCsv(): String = withContext(Dispatchers.IO) {
        val completedWorkouts = workoutDao.getCompletedWorkouts().first()
        val sb = StringBuilder()
        // CSV Header
        sb.append("Date,Workout Title,Exercise Name,Set,Weight (kg),Reps,Set Type,RPE,Completed\n")

        for (w in completedWorkouts) {
            val dateStr = csvDateFormat.format(Date(w.workout.startTime))
            val workoutTitle = escapeCsv(w.workout.title)

            for (we in w.exercises) {
                val exerciseName = escapeCsv(we.exercise.name)
                for (s in we.sets) {
                    sb.append(dateStr).append(",")
                    sb.append(workoutTitle).append(",")
                    sb.append(exerciseName).append(",")
                    sb.append(s.setNumber).append(",")
                    sb.append(s.weight).append(",")
                    sb.append(s.reps).append(",")
                    sb.append(s.setType.displayNameRu).append(",")
                    sb.append(s.rpe?.toString() ?: "").append(",")
                    sb.append(if (s.isCompleted) "1" else "0").append("\n")
                }
            }
        }

        sb.toString()
    }

    /**
     * Импорт данных из JSON файла
     */
    suspend fun importBackupJson(jsonString: String): Result<ImportSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject(jsonString)
            var profileRestored = false
            var templatesCount = 0
            var workoutsCount = 0

            // 1. Восстановление профиля
            if (root.has("profile")) {
                val pObj = root.getJSONObject("profile")
                val entity = UserProfileEntity(
                    id = pObj.optString("id", "default_user"),
                    name = pObj.optString("name", "Атлет"),
                    gender = try { Gender.valueOf(pObj.getString("gender")) } catch (e: Exception) { Gender.MALE },
                    age = pObj.optInt("age", 25),
                    heightCm = pObj.optDouble("heightCm", 175.0).toFloat(),
                    weightKg = pObj.optDouble("weightKg", 75.0).toFloat(),
                    experience = try { TrainingExperience.valueOf(pObj.getString("experience")) } catch (e: Exception) { TrainingExperience.INTERMEDIATE },
                    primaryGoal = try { FitnessGoal.valueOf(pObj.getString("primaryGoal")) } catch (e: Exception) { FitnessGoal.HYPERTROPHY },
                    workoutsPerWeek = pObj.optInt("workoutsPerWeek", 3),
                    isCompleted = pObj.optBoolean("isCompleted", true),
                    role = try { com.gym.core.model.UserRole.valueOf(pObj.optString("role", "ATHLETE")) } catch (e: Exception) { com.gym.core.model.UserRole.ATHLETE },
                    email = pObj.optString("email").takeIf { it.isNotBlank() },
                    isGuest = pObj.optBoolean("isGuest", true),
                    coachInviteCode = pObj.optString("coachInviteCode", "APEX77"),
                    linkedCoachCode = pObj.optString("linkedCoachCode").takeIf { it.isNotBlank() }
                )
                userProfileDao.saveUserProfile(entity)
                profileRestored = true
            }

            // 2. Восстановление шаблонов
            if (root.has("templates")) {
                val tArray = root.getJSONArray("templates")
                for (i in 0 until tArray.length()) {
                    val tObj = tArray.getJSONObject(i)
                    val title = tObj.getString("title")
                    val desc = tObj.optString("description", "")
                    val level = try {
                        TrainingExperience.valueOf(tObj.optString("experienceLevel", "INTERMEDIATE"))
                    } catch (e: Exception) {
                        TrainingExperience.INTERMEDIATE
                    }
                    val createdAt = tObj.optLong("createdAt", System.currentTimeMillis())

                    val templateId = templateDao.insertTemplate(
                        WorkoutTemplateEntity(
                            title = title,
                            description = desc,
                            experienceLevel = level,
                            createdAt = createdAt
                        )
                    )

                    val exArray = tObj.optJSONArray("exercises")
                    if (exArray != null) {
                        val exEntities = mutableListOf<TemplateExerciseEntity>()
                        for (j in 0 until exArray.length()) {
                            val eObj = exArray.getJSONObject(j)
                            exEntities.add(
                                TemplateExerciseEntity(
                                    templateId = templateId,
                                    exerciseId = eObj.getLong("exerciseId"),
                                    orderIndex = eObj.optInt("orderIndex", j),
                                    supersetId = eObj.optString("supersetId").takeIf { it.isNotBlank() },
                                    targetSetsCount = eObj.optInt("targetSetsCount", 3),
                                    defaultRestSeconds = eObj.optInt("defaultRestSeconds", 90)
                                )
                            )
                        }
                        if (exEntities.isNotEmpty()) {
                            templateDao.insertTemplateExercises(exEntities)
                        }
                    }
                    templatesCount++
                }
            }

            // 3. Восстановление истории тренировок
            if (root.has("workouts")) {
                val wArray = root.getJSONArray("workouts")
                for (i in 0 until wArray.length()) {
                    val wObj = wArray.getJSONObject(i)
                    val title = wObj.getString("title")
                    val startTime = wObj.optLong("startTime", System.currentTimeMillis())
                    val endTime = wObj.optLong("endTime", startTime + 3600_000L)
                    val duration = wObj.optLong("durationSeconds", (endTime - startTime) / 1000)
                    val notes = wObj.optString("notes", "")

                    val workoutId = workoutDao.insertWorkout(
                        WorkoutEntity(
                            title = title,
                            startTime = startTime,
                            endTime = endTime,
                            durationSeconds = duration,
                            status = WorkoutStatus.COMPLETED,
                            notes = notes
                        )
                    )

                    val exListArray = wObj.optJSONArray("exercises")
                    if (exListArray != null) {
                        for (j in 0 until exListArray.length()) {
                            val weObj = exListArray.getJSONObject(j)
                            val workoutExerciseId = workoutDao.insertWorkoutExercise(
                                WorkoutExerciseEntity(
                                    workoutId = workoutId,
                                    exerciseId = weObj.getLong("exerciseId"),
                                    orderIndex = weObj.optInt("orderIndex", j),
                                    supersetId = weObj.optString("supersetId").takeIf { it.isNotBlank() }
                                )
                            )

                            val setsArray = weObj.optJSONArray("sets")
                            if (setsArray != null) {
                                val setsList = mutableListOf<WorkoutSetEntity>()
                                for (k in 0 until setsArray.length()) {
                                    val sObj = setsArray.getJSONObject(k)
                                    val setType = try {
                                        SetType.valueOf(sObj.optString("setType", "NORMAL"))
                                    } catch (e: Exception) {
                                        SetType.NORMAL
                                    }
                                    val rpeVal = if (sObj.has("rpe") && sObj.getDouble("rpe") > 0.0) {
                                        sObj.getDouble("rpe").toFloat()
                                    } else null

                                    val effort = try {
                                        sObj.optString("effort").takeIf { it.isNotBlank() }?.let { PerceivedEffort.valueOf(it) }
                                    } catch (e: Exception) {
                                        null
                                    }
                                    setsList.add(
                                        WorkoutSetEntity(
                                            workoutExerciseId = workoutExerciseId,
                                            setNumber = sObj.optInt("setNumber", k + 1),
                                            setType = setType,
                                            weight = sObj.optDouble("weight", 0.0),
                                            reps = sObj.optInt("reps", 0),
                                            rpe = rpeVal,
                                            effort = effort,
                                            isCompleted = sObj.optBoolean("isCompleted", true),
                                            completedAt = sObj.optLong("completedAt", startTime)
                                        )
                                    )
                                }
                                if (setsList.isNotEmpty()) {
                                    workoutDao.insertWorkoutSets(setsList)
                                }
                            }
                        }
                    }
                    workoutsCount++
                }
            }

            ImportSummary(
                templatesRestored = templatesCount,
                workoutsRestored = workoutsCount,
                profileRestored = profileRestored
            )
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
