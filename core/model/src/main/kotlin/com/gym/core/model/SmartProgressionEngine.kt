package com.gym.core.model

enum class ProgressionDecision {
    INCREASE_WEIGHT,
    MAINTAIN_WEIGHT,
    DECREASE_WEIGHT
}

data class ProgressionRecommendation(
    val suggestedWeight: Double,
    val suggestedReps: String,
    val decision: ProgressionDecision,
    val rationaleRu: String
)

object SmartProgressionEngine {

    /**
     * Вычисляет адаптивную прогрессию веса на основе Двойной прогрессии (Double Progression).
     *
     * @param previousSets Выполненные подходы в прошлый раз
     * @param targetMinReps Нижняя граница целевого диапазона (напр. 8)
     * @param targetMaxReps Верхняя граница целевого диапазона (напр. 10)
     * @param equipmentProfile Физический профиль оборудования с шагом весов
     */
    fun calculateNextLoad(
        previousSets: List<WorkoutSet>,
        targetMinReps: Int = 8,
        targetMaxReps: Int = 10,
        equipmentProfile: EquipmentWeightProfile
    ): ProgressionRecommendation {
        val workingSets = previousSets.filter { it.setType != SetType.WARMUP && it.isCompleted }
        if (workingSets.isEmpty()) {
            val baseWeight = equipmentProfile.snapWeight(20.0)
            return ProgressionRecommendation(
                suggestedWeight = baseWeight,
                suggestedReps = "$targetMinReps-$targetMaxReps",
                decision = ProgressionDecision.MAINTAIN_WEIGHT,
                rationaleRu = "Первая тренировка: начните с базового веса и отработайте технику."
            )
        }

        val lastWeight = workingSets.first().weight
        val allHitMaxReps = workingSets.all { it.reps >= targetMaxReps }
        val anyFailedMinReps = workingSets.any { it.reps < targetMinReps }

        return when {
            // Успех: все сеты закрыты на максимум повторов -> повышаем вес
            allHitMaxReps -> {
                // Инкремент веса: пробуем прибавить сырой шаг и округлить до физического шага снаряда
                val step = when (equipmentProfile.equipment) {
                    Equipment.DUMBBELL -> if (lastWeight < equipmentProfile.smallWeightThreshold) equipmentProfile.smallWeightStep else equipmentProfile.defaultStep
                    Equipment.BARBELL -> equipmentProfile.minPlatesPairWeight
                    Equipment.CABLE, Equipment.MACHINE -> equipmentProfile.defaultStep
                    else -> 1.0
                }

                val targetCandidate = lastWeight + step
                val nextWeight = equipmentProfile.snapWeight(targetCandidate)
                val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight

                ProgressionRecommendation(
                    suggestedWeight = finalWeight,
                    suggestedReps = "$targetMinReps",
                    decision = ProgressionDecision.INCREASE_WEIGHT,
                    rationaleRu = "Отличная работа! Вы закрыли все $targetMaxReps повторений в каждом подходе. Время повысить вес до $finalWeight кг (шаг снаряда)."
                )
            }

            // Перегруз: атлет не дотянул даже до минимальных повторений -> снижаем вес
            anyFailedMinReps -> {
                val step = when (equipmentProfile.equipment) {
                    Equipment.DUMBBELL -> if (lastWeight <= equipmentProfile.smallWeightThreshold) equipmentProfile.smallWeightStep else equipmentProfile.defaultStep
                    Equipment.BARBELL -> equipmentProfile.minPlatesPairWeight
                    Equipment.CABLE, Equipment.MACHINE -> equipmentProfile.defaultStep
                    else -> 1.0
                }

                val reduced = (lastWeight - step).coerceAtLeast(0.0)
                val nextWeight = equipmentProfile.snapWeight(reduced)

                ProgressionRecommendation(
                    suggestedWeight = nextWeight,
                    suggestedReps = "$targetMinReps-$targetMaxReps",
                    decision = ProgressionDecision.DECREASE_WEIGHT,
                    rationaleRu = "Нагрузка была слишком высокой (< $targetMinReps повторений). Рекомендуем снизить вес до $nextWeight кг для чистоты техники."
                )
            }

            // Рабочий процесс: находимся внутри диапазона -> держим вес, растим повторения
            else -> {
                val avgReps = (workingSets.map { it.reps }.average() * 10).toInt() / 10.0
                ProgressionRecommendation(
                    suggestedWeight = lastWeight,
                    suggestedReps = "$targetMinReps-$targetMaxReps",
                    decision = ProgressionDecision.MAINTAIN_WEIGHT,
                    rationaleRu = "Оставьте $lastWeight кг. Задача на сегодня: прибавить 1-2 повторения в отстающих подходах до достижения $targetMaxReps повторов."
                )
            }
        }
    }
}
