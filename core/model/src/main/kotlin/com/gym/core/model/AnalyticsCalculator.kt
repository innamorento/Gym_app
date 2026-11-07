package com.gym.core.model

import java.util.Date
import kotlin.math.roundToInt

data class OneRmTrendPoint(
    val workoutDateMillis: Long,
    val weight: Double,
    val reps: Int,
    val estimated1Rm: Double,
    val isPersonalRecord: Boolean
)

data class WeeklyVolumePoint(
    val weekLabel: String,
    val weekStartMillis: Long,
    val totalVolumeKg: Double,
    val totalSets: Int
)

data class MuscleGroupBalance(
    val pushVolumeKg: Double,
    val pullVolumeKg: Double,
    val legsVolumeKg: Double,
    val coreOtherVolumeKg: Double,
    val pushPercentage: Float,
    val pullPercentage: Float,
    val legsPercentage: Float,
    val balanceStatus: BalanceStatus,
    val recommendation: String
)

enum class BalanceStatus(val titleRu: String) {
    OPTIMAL("Оптимальный баланс"),
    PUSH_DOMINANT("Преобладание жимовых"),
    PULL_DEFICIT("Дефицит тяговых движений"),
    LEGS_DEFICIT("Недостаток нагрузки на ноги"),
    NEEDS_MORE_DATA("Мало данных")
}

data class CompoundPrTrophy(
    val exerciseName: String,
    val best1Rm: Double,
    val maxWeight: Double,
    val maxWeightReps: Int,
    val max3RepsWeight: Double? = null,
    val max5RepsWeight: Double? = null,
    val max10RepsWeight: Double? = null,
    val lastAchievedDateMillis: Long
)

object AnalyticsCalculator {

    /**
     * Расчет динамики 1RM с флагом новых личных рекордов (PR) в хронологическом порядке.
     */
    fun calculate1RmTrend(
        exerciseId: Long,
        completedWorkouts: List<Workout>
    ): List<OneRmTrendPoint> {
        val sortedWorkouts = completedWorkouts.sortedBy { it.startTime }
        val points = mutableListOf<OneRmTrendPoint>()
        var currentAllTimeBest1Rm = 0.0

        for (workout in sortedWorkouts) {
            val matchingExercises = workout.exercises.filter { it.exercise.id == exerciseId }
            var bestSet1RmInWorkout = 0.0
            var bestWeightInWorkout = 0.0
            var bestRepsInWorkout = 0

            for (we in matchingExercises) {
                for (set in we.sets.filter { it.isCompleted && it.setType != SetType.WARMUP }) {
                    val rm = set.estimated1RM
                    if (rm > bestSet1RmInWorkout) {
                        bestSet1RmInWorkout = rm
                        bestWeightInWorkout = set.weight
                        bestRepsInWorkout = set.reps
                    }
                }
            }

            if (bestSet1RmInWorkout > 0.0) {
                val isPr = bestSet1RmInWorkout > currentAllTimeBest1Rm
                if (isPr) {
                    currentAllTimeBest1Rm = bestSet1RmInWorkout
                }

                points.add(
                    OneRmTrendPoint(
                        workoutDateMillis = workout.startTime,
                        weight = bestWeightInWorkout,
                        reps = bestRepsInWorkout,
                        estimated1Rm = (bestSet1RmInWorkout * 10.0).roundToInt() / 10.0,
                        isPersonalRecord = isPr
                    )
                )
            }
        }

        return points
    }

    /**
     * Группировка суммарного тоннажа и подходов по неделям (последние N недель).
     */
    fun calculateWeeklyVolume(
        completedWorkouts: List<Workout>,
        numberOfWeeks: Int = 4
    ): List<WeeklyVolumePoint> {
        if (completedWorkouts.isEmpty()) return emptyList()

        val oneWeekMillis = 7L * 24 * 3600 * 1000
        val now = System.currentTimeMillis()
        val result = mutableListOf<WeeklyVolumePoint>()

        for (i in (numberOfWeeks - 1) downTo 0) {
            val weekStart = now - (i + 1) * oneWeekMillis
            val weekEnd = now - i * oneWeekMillis

            val weekWorkouts = completedWorkouts.filter { it.startTime in weekStart until weekEnd }
            val totalVolume = weekWorkouts.sumOf { it.totalVolume }
            val totalSets = weekWorkouts.sumOf { it.totalCompletedSets }

            val label = when (i) {
                0 -> "Текущая"
                1 -> "Прошлая"
                else -> "${i} нед. назад"
            }

            result.add(
                WeeklyVolumePoint(
                    weekLabel = label,
                    weekStartMillis = weekStart,
                    totalVolumeKg = (totalVolume * 10.0).roundToInt() / 10.0,
                    totalSets = totalSets
                )
            )
        }

        return result
    }

    /**
     * Оценка баланса мышечных групп (Push / Pull / Legs).
     */
    fun calculateMuscleBalance(
        completedWorkouts: List<Workout>
    ): MuscleGroupBalance {
        var pushVol = 0.0
        var pullVol = 0.0
        var legsVol = 0.0
        var coreOtherVol = 0.0

        for (workout in completedWorkouts) {
            for (exercise in workout.exercises) {
                val cat = MuscleCategory.categoryFor(exercise.exercise.primaryMuscleGroup)
                val workingSets = exercise.sets.filter { it.isCompleted && it.setType != SetType.WARMUP }
                val vol = workingSets.sumOf { it.volume }

                when (cat) {
                    MuscleCategory.CHEST, MuscleCategory.SHOULDERS, MuscleCategory.TRICEPS -> pushVol += vol
                    MuscleCategory.BACK, MuscleCategory.BICEPS -> pullVol += vol
                    MuscleCategory.LEGS -> legsVol += vol
                    else -> coreOtherVol += vol
                }
            }
        }

        val total = pushVol + pullVol + legsVol + coreOtherVol
        if (total <= 0.0) {
            return MuscleGroupBalance(
                pushVolumeKg = 0.0,
                pullVolumeKg = 0.0,
                legsVolumeKg = 0.0,
                coreOtherVolumeKg = 0.0,
                pushPercentage = 0f,
                pullPercentage = 0f,
                legsPercentage = 0f,
                balanceStatus = BalanceStatus.NEEDS_MORE_DATA,
                recommendation = "Проведите еще 2-3 тренировки для формирования точной оценки баланса мышц."
            )
        }

        val pushPct = ((pushVol / total) * 100).toFloat()
        val pullPct = ((pullVol / total) * 100).toFloat()
        val legsPct = ((legsVol / total) * 100).toFloat()

        val (status, rec) = when {
            legsPct < 20f && total > 5000 -> Pair(
                BalanceStatus.LEGS_DEFICIT,
                "Объем на ноги ниже 20%. Добавьте приседания или жим ногами для гармоничного развития."
            )
            pushPct > pullPct * 1.5f && total > 5000 -> Pair(
                BalanceStatus.PUSH_DOMINANT,
                "Жимовой объем превышает тяговый в 1.5+ раза. Уделите внимание спине и задней дельте для осанки."
            )
            pullPct < 20f && total > 5000 -> Pair(
                BalanceStatus.PULL_DEFICIT,
                "Недостаток тяговых движений. Добавьте подтягивания или тягу штанги в наклоне."
            )
            else -> Pair(
                BalanceStatus.OPTIMAL,
                "Баланс нагрузок Push / Pull / Legs находится в идеальной пропорции для гипертрофии и здоровья суставов."
            )
        }

        return MuscleGroupBalance(
            pushVolumeKg = (pushVol * 10.0).roundToInt() / 10.0,
            pullVolumeKg = (pullVol * 10.0).roundToInt() / 10.0,
            legsVolumeKg = (legsVol * 10.0).roundToInt() / 10.0,
            coreOtherVolumeKg = (coreOtherVol * 10.0).roundToInt() / 10.0,
            pushPercentage = (pushPct * 10).roundToInt() / 10f,
            pullPercentage = (pullPct * 10).roundToInt() / 10f,
            legsPercentage = (legsPct * 10).roundToInt() / 10f,
            balanceStatus = status,
            recommendation = rec
        )
    }

    /**
     * Поиск рекордов в Зал Славы (PR Trophies) для базовых упражнений.
     */
    fun findCompoundTrophies(
        completedWorkouts: List<Workout>
    ): List<CompoundPrTrophy> {
        val compoundKeywords = listOf(
            "Жим лежа" to listOf("жим лежа", "жим штанги лежа", "bench press"),
            "Приседания" to listOf("приседания", "присед", "squat"),
            "Становая тяга" to listOf("становая тяга", "тяга становая", "deadlift"),
            "Армейский жим" to listOf("армейский жим", "жим стоя", "overhead press", "military press"),
            "Подтягивания" to listOf("подтягивания", "pull up", "chin up")
        )

        val trophies = mutableListOf<CompoundPrTrophy>()

        for ((displayName, searchTerms) in compoundKeywords) {
            var best1Rm = 0.0
            var maxWeight = 0.0
            var maxWeightReps = 0
            var max3RepsWeight: Double? = null
            var max5RepsWeight: Double? = null
            var max10RepsWeight: Double? = null
            var lastAchievedDate = 0L

            for (workout in completedWorkouts) {
                for (we in workout.exercises) {
                    val exNameLower = we.exercise.name.lowercase()
                    val matches = searchTerms.any { exNameLower.contains(it) }
                    if (!matches) continue

                    for (set in we.sets.filter { it.isCompleted && it.setType != SetType.WARMUP }) {
                        val rm = set.estimated1RM
                        if (rm > best1Rm) {
                            best1Rm = rm
                            lastAchievedDate = workout.startTime
                        }
                        if (set.weight > maxWeight) {
                            maxWeight = set.weight
                            maxWeightReps = set.reps
                        }
                        if (set.reps >= 3 && (max3RepsWeight == null || set.weight > max3RepsWeight)) {
                            max3RepsWeight = set.weight
                        }
                        if (set.reps >= 5 && (max5RepsWeight == null || set.weight > max5RepsWeight)) {
                            max5RepsWeight = set.weight
                        }
                        if (set.reps >= 10 && (max10RepsWeight == null || set.weight > max10RepsWeight)) {
                            max10RepsWeight = set.weight
                        }
                    }
                }
            }

            if (best1Rm > 0.0) {
                trophies.add(
                    CompoundPrTrophy(
                        exerciseName = displayName,
                        best1Rm = (best1Rm * 10.0).roundToInt() / 10.0,
                        maxWeight = maxWeight,
                        maxWeightReps = maxWeightReps,
                        max3RepsWeight = max3RepsWeight,
                        max5RepsWeight = max5RepsWeight,
                        max10RepsWeight = max10RepsWeight,
                        lastAchievedDateMillis = lastAchievedDate
                    )
                )
            }
        }

        return trophies
    }
}
