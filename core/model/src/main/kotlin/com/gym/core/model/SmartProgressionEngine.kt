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
    val rationaleRu: String,
    val goalTitle: String = "",
    val goalEmoji: String = ""
)

object SmartProgressionEngine {

    /**
     * Возвращает научно обоснованный диапазон повторений в зависимости от цели атлета:
     * - Сила: 4..6 (высокая интенсивность, нейромышечная адаптация)
     * - Гипертрофия: 8..12 (оптимальное механическое напряжение и объем)
     * - Похудение: 10..15 (метаболическая плотность и сохранение мышц на дефиците)
     * - Здоровье: 10..12 (безопасность суставов и выносливость)
     */
    fun targetRepRangeForGoal(goal: FitnessGoal): Pair<Int, Int> = when (goal) {
        FitnessGoal.STRENGTH -> 4 to 6
        FitnessGoal.HYPERTROPHY -> 8 to 12
        FitnessGoal.FAT_LOSS -> 10 to 15
        FitnessGoal.HEALTH_FITNESS -> 10 to 12
    }

    /**
     * Вычисляет адаптивную прогрессию нагрузки на основе цели атлета,
     * физического шага снаряда и субъективной шкалы усилия (RPE / PerceivedEffort).
     */
    fun calculateGoalAwareLoad(
        previousSets: List<WorkoutSet>,
        equipmentProfile: EquipmentWeightProfile,
        goal: FitnessGoal = FitnessGoal.HYPERTROPHY,
        experience: TrainingExperience = TrainingExperience.INTERMEDIATE
    ): ProgressionRecommendation {
        val (minReps, maxReps) = targetRepRangeForGoal(goal)
        val workingSets = previousSets.filter { it.setType != SetType.WARMUP && it.isCompleted }

        if (workingSets.isEmpty()) {
            val baseWeight = equipmentProfile.snapWeight(if (goal == FitnessGoal.STRENGTH) 30.0 else 20.0)
            return ProgressionRecommendation(
                suggestedWeight = baseWeight,
                suggestedReps = "$minReps-$maxReps",
                decision = ProgressionDecision.MAINTAIN_WEIGHT,
                rationaleRu = "Первая тренировка по программе: начните с базового веса $baseWeight кг и отработайте технику в диапазоне $minReps-$maxReps повторений.",
                goalTitle = goal.displayNameRu,
                goalEmoji = goal.emoji
            )
        }

        val lastWeight = workingSets.first().weight
        val allHitMaxReps = workingSets.all { it.reps >= maxReps }
        val anyFailedMinReps = workingSets.any { it.reps < minReps }
        val reachedFailure = workingSets.any { it.effort == PerceivedEffort.FAILURE }
        val predominantlyHard = workingSets.count { it.effort == PerceivedEffort.HARD || it.effort == PerceivedEffort.FAILURE } >= 2
        val allEffortEasy = workingSets.all { it.effort == PerceivedEffort.EASY }

        val step = when (equipmentProfile.equipment) {
            Equipment.DUMBBELL -> if (lastWeight < equipmentProfile.smallWeightThreshold) equipmentProfile.smallWeightStep else equipmentProfile.defaultStep
            Equipment.BARBELL -> equipmentProfile.minPlatesPairWeight
            Equipment.CABLE, Equipment.MACHINE -> equipmentProfile.defaultStep
            else -> 1.0
        }

        return when (goal) {
            FitnessGoal.HYPERTROPHY -> {
                when {
                    // Все подходы закрыли 12+ повторений без предельного отказа
                    allHitMaxReps && !reachedFailure && !predominantlyHard -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Набор массы. Вы закрыли верхнюю планку $maxReps повторений без отказа! Двойная прогрессия: увеличиваем вес до $finalWeight кг и возвращаемся к $minReps повторам для нового витка гипертрофии.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    allHitMaxReps && (reachedFailure || predominantlyHard) -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Набор массы. Верхняя планка $maxReps повт. достигнута, но с предельным усилием (Отказ / Тяжело). Закрепите этот вес еще на 1 тренировку для связочной адаптации.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    anyFailedMinReps -> {
                        val reduced = (lastWeight - step).coerceAtLeast(0.0)
                        val nextWeight = equipmentProfile.snapWeight(reduced)
                        ProgressionRecommendation(
                            suggestedWeight = nextWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.DECREASE_WEIGHT,
                            rationaleRu = "Нагрузка выше оптимального диапазона гипертрофии (< $minReps повторов). Рекомендуем снизить вес до $nextWeight кг для целевого механического напряжения без читинга.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    else -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Набор массы. Рабочий диапазон. Держите $lastWeight кг и добавьте по 1-2 повтора в каждом сете, стремясь заполнить все подходы до $maxReps повт.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                }
            }

            FitnessGoal.STRENGTH -> {
                when {
                    // В силовом режиме приоритет - рост интенсивности при сохранении запаса (RIR >= 1-2)
                    allHitMaxReps && (allEffortEasy || workingSets.all { it.effort == PerceivedEffort.OPTIMAL }) -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Развитие силы. Превосходная нервно-мышечная адаптация с запасом повторений (RPE в цель). Увеличиваем рабочий вес до $finalWeight кг.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    reachedFailure || predominantlyHard -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Развитие силы. Подход выполнен на околопредельном усилии (RPE 9-10). Во избежание перегрузки ЦНС и связок закрепите $lastWeight кг перед следующим шагом.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    anyFailedMinReps -> {
                        val reduced = (lastWeight - step).coerceAtLeast(0.0)
                        val nextWeight = equipmentProfile.snapWeight(reduced)
                        ProgressionRecommendation(
                            suggestedWeight = nextWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.DECREASE_WEIGHT,
                            rationaleRu = "Вес слишком велик (< $minReps повторений). Снизьте нагрузку до $nextWeight кг для чистоты траектории и сохранения взрывной скорости снаряда.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    else -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Развитие силы. Оставьте $lastWeight кг и добейтесь уверенных $maxReps повторений в каждом сете.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                }
            }

            FitnessGoal.FAT_LOSS -> {
                when {
                    // На дефиците калорий восстановление снижено: вес повышаем ТОЛЬКО если легко
                    allHitMaxReps && allEffortEasy -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Похудение и рельеф. Подходы дались очень легко. Умеренно повышаем вес до $finalWeight кг при сохранении высокой плотности тренировки.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    anyFailedMinReps -> {
                        val reduced = (lastWeight - step).coerceAtLeast(0.0)
                        val nextWeight = equipmentProfile.snapWeight(reduced)
                        ProgressionRecommendation(
                            suggestedWeight = nextWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.DECREASE_WEIGHT,
                            rationaleRu = "Цель: Похудение. Падение повторов ниже $minReps указывает на утомление. Снизьте вес до $nextWeight кг для поддержания высокого метаболического темпа.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    else -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Похудение и рельеф. Главная задача на дефиците — удерживать силовые показатели ($lastWeight кг) и сохранять сухую мышечную массу.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                }
            }

            FitnessGoal.HEALTH_FITNESS -> {
                when {
                    allHitMaxReps && (allEffortEasy || workingSets.all { it.effort == PerceivedEffort.OPTIMAL }) -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Здоровье и тонус. Организм отлично справляется. Мягко повышаем нагрузку до $finalWeight кг для укрепления мышечного корсета.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    reachedFailure || anyFailedMinReps -> {
                        val reduced = (lastWeight - step).coerceAtLeast(0.0)
                        val nextWeight = equipmentProfile.snapWeight(reduced)
                        ProgressionRecommendation(
                            suggestedWeight = nextWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.DECREASE_WEIGHT,
                            rationaleRu = "Цель: Здоровье. Тренировки до отказа не рекомендуются. Снизьте вес до $nextWeight кг для комфортной работы суставов.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                    else -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Здоровье и тонус. Оставьте $lastWeight кг. Фокусируйтесь на ровном дыхании и идеальной траектории движения.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji
                        )
                    }
                }
            }
        }
    }

    /**
     * Базовый расчет по фиксированному диапазону повторений (для обратной совместимости).
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
        val reachedFailure = workingSets.any { it.effort == PerceivedEffort.FAILURE }
        val predominantlyHard = workingSets.count { it.effort == PerceivedEffort.HARD || it.effort == PerceivedEffort.FAILURE } >= 2

        return when {
            allHitMaxReps && (reachedFailure || predominantlyHard) -> {
                ProgressionRecommendation(
                    suggestedWeight = lastWeight,
                    suggestedReps = "$targetMaxReps",
                    decision = ProgressionDecision.MAINTAIN_WEIGHT,
                    rationaleRu = "Вы закрыли все $targetMaxReps повторений, но с предельным усилием (Отказ / Тяжело). Повышать вес рано: закрепите этот вес еще на 1 тренировку для безопасной адаптации связок."
                )
            }

            allHitMaxReps -> {
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
                    rationaleRu = "Отличная работа! Подходы выполнены уверенно (усилие в цель). Время повысить вес до $finalWeight кг (шаг снаряда)."
                )
            }

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

            else -> {
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
