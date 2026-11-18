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
    val goalEmoji: String = "",
    val goal: FitnessGoal? = null
)

object SmartProgressionEngine {

    // [START:TASK-018] Dynamic 1RM calculation via Epley formula
    fun estimate1Rm(weight: Double, reps: Int): Double =
        if (weight <= 0.0 || reps <= 0) 0.0 else weight * (1.0 + reps / 30.0)

    fun estimateTargetWeightForReps(oneRepMax: Double, targetReps: Int): Double =
        if (oneRepMax <= 0.0 || targetReps <= 0) 0.0 else oneRepMax / (1.0 + targetReps / 30.0)
    // [END:TASK-018]

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
            val rawStarting = when (equipmentProfile.equipment) {
                Equipment.DUMBBELL -> if (goal == FitnessGoal.STRENGTH) 12.0 else 8.0
                Equipment.BARBELL -> if (goal == FitnessGoal.STRENGTH) 30.0 else equipmentProfile.barWeight
                Equipment.CABLE -> if (goal == FitnessGoal.STRENGTH) 20.0 else 12.5
                Equipment.MACHINE -> if (goal == FitnessGoal.STRENGTH) 25.0 else 15.0
                Equipment.BODYWEIGHT -> 0.0
                else -> 10.0
            }
            val baseWeight = equipmentProfile.snapWeight(rawStarting)
            return ProgressionRecommendation(
                suggestedWeight = baseWeight,
                suggestedReps = "$minReps-$maxReps",
                decision = ProgressionDecision.MAINTAIN_WEIGHT,
                rationaleRu = "Первая тренировка по программе: начните с базового веса $baseWeight кг и отработайте технику в диапазоне $minReps-$maxReps повторений.",
                goalTitle = goal.displayNameRu,
                goalEmoji = goal.emoji, goal = goal
            )
        }

        val lastSet = workingSets.last()
        val lastWeight = lastSet.weight
        val lastReps = lastSet.reps
        val lastEffort = lastSet.effort
        val allHitMaxReps = workingSets.all { it.reps >= maxReps }
        val anyFailedMinReps = workingSets.any { it.reps < minReps }
        val reachedFailure = workingSets.any { it.effort == PerceivedEffort.FAILURE }
        val hasHardSet = workingSets.any { it.effort == PerceivedEffort.HARD }
        val predominantlyHard = workingSets.count { it.effort == PerceivedEffort.HARD || it.effort == PerceivedEffort.FAILURE } >= 1
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
                    // [AI-TASK:TASK-018] 1RM recalibration for hypertrophic overshot reps
                    lastReps >= maxReps + 3 && !reachedFailure -> {
                        val estimated1Rm = estimate1Rm(lastWeight, lastReps)
                        val targetMidpoint = (minReps + maxReps) / 2
                        val calculatedWeight = estimateTargetWeightForReps(estimated1Rm, targetMidpoint)
                        val snappedTarget = equipmentProfile.snapWeight(calculatedWeight)
                        val finalWeight = maxOf(snappedTarget, equipmentProfile.snapWeight(lastWeight + step))
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Набор массы. Выполнено $lastReps повт. с весом $lastWeight кг (расчетный 1ПМ ≈ ${kotlin.math.round(estimated1Rm).toInt()} кг). Снаряд слишком легкий для гипертрофии ($minReps-$maxReps повт.). Научная калибровка 1RM: повышаем вес до $finalWeight кг под диапазон $minReps-$maxReps повт.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    // Все подходы закрыли 12+ повторений без предельного отказа
                    allHitMaxReps && !reachedFailure && !predominantlyHard -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Набор массы. Вы закрыли верхнюю планку ($lastReps повт.) без отказа! Двойная прогрессия: увеличиваем вес до $finalWeight кг и возвращаемся к $minReps повторам для нового витка гипертрофии.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    // [AI-TASK:TASK-022] RPE live autoregulation on single/first sets
                    reachedFailure || lastEffort == PerceivedEffort.HARD -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = if (reachedFailure) {
                                "Цель: Набор массы. Вы отметили отказ (RIR 0). Зафиксируйте $lastWeight кг или снизьте вес на следующий подход для сохранения целевого объема."
                            } else {
                                "Цель: Набор массы. Вы отметили: Тяжело (запас 1 повт., RPE 9). Нагрузка предельная, зафиксируйте $lastWeight кг и удержите правильную амплитуду."
                            },
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    lastEffort == PerceivedEffort.EASY && !anyFailedMinReps -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Набор массы. Вы отметили: Легко (запас 3+ повт.). Снаряд $lastWeight кг не создает достаточного стимула. Рекомендуем добавить вес до $finalWeight кг.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
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
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    else -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Набор массы. Рабочий диапазон. Держите $lastWeight кг и добавьте по 1-2 повтора в каждом сете, стремясь заполнить все подходы до $maxReps повт.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                }
            }

            FitnessGoal.STRENGTH -> {
                when {
                    // [AI-TASK:TASK-018] 1RM recalibration for strength overshot reps (e.g. 40kg x 12 -> 47.5kg)
                    lastReps >= maxReps + 2 && !reachedFailure -> {
                        val estimated1Rm = estimate1Rm(lastWeight, lastReps)
                        val targetMidpoint = (minReps + maxReps) / 2
                        val calculatedWeight = estimateTargetWeightForReps(estimated1Rm, targetMidpoint)
                        val snappedTarget = equipmentProfile.snapWeight(calculatedWeight)
                        val finalWeight = maxOf(snappedTarget, equipmentProfile.snapWeight(lastWeight + step))
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Развитие силы. Выполнено $lastReps повт. с весом $lastWeight кг (расчетный 1ПМ ≈ ${kotlin.math.round(estimated1Rm).toInt()} кг). Количество повторений существенно превысило силовую зону $minReps-$maxReps. Адаптивная калибровка 1RM: рекомендуемый вес повышен до $finalWeight кг под диапазон $minReps-$maxReps повт.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    // В силовом режиме приоритет - рост интенсивности при сохранении запаса (RIR >= 1-2)
                    allHitMaxReps && (allEffortEasy || workingSets.all { it.effort == PerceivedEffort.OPTIMAL }) -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = if (allEffortEasy) {
                                "Цель: Развитие силы. Подход выполнен с запасом (Легко, RIR 3+). Увеличиваем рабочий вес до $finalWeight кг под диапазон $minReps-$maxReps повт."
                            } else {
                                "Цель: Развитие силы. Превосходная нервно-мышечная адаптация с запасом повторений (RPE в цель). Увеличиваем рабочий вес до $finalWeight кг под диапазон $minReps-$maxReps повт."
                            },
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    // [AI-TASK:TASK-022] RPE live autoregulation for strength
                    reachedFailure || lastEffort == PerceivedEffort.HARD -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = when {
                                reachedFailure -> "Цель: Развитие силы. Вы отметили отказ (RIR 0). Во избежание перегрузки ЦНС и связок закрепите $lastWeight кг или снизьте вес на 5-10%."
                                lastEffort == PerceivedEffort.HARD -> "Цель: Развитие силы. Вы отметили: Тяжело (запас 1 повт., RPE 9). Подход выполнен на околопредельном усилии. Вес не повышаем, зафиксируйте $lastWeight кг и отдохните 2-3 минуты."
                                else -> "Цель: Развитие силы. Подход выполнен на околопредельном усилии. Зафиксируйте $lastWeight кг."
                            },
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    lastEffort == PerceivedEffort.EASY && !anyFailedMinReps -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Развитие силы. Вы отметили: Легко (запас 3+ повт.). Снаряд идет с большим запасом — повышаем вес до $finalWeight кг.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
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
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    else -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Развитие силы. Оставьте $lastWeight кг и добейтесь уверенных $maxReps повторений в каждом сете.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                }
            }

            FitnessGoal.FAT_LOSS -> {
                when {
                    lastReps >= maxReps + 3 && !reachedFailure -> {
                        val estimated1Rm = estimate1Rm(lastWeight, lastReps)
                        val targetMidpoint = (minReps + maxReps) / 2
                        val calculatedWeight = estimateTargetWeightForReps(estimated1Rm, targetMidpoint)
                        val snappedTarget = equipmentProfile.snapWeight(calculatedWeight)
                        val finalWeight = maxOf(snappedTarget, equipmentProfile.snapWeight(lastWeight + step))
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Похудение и рельеф. Выполнено $lastReps повт. с весом $lastWeight кг (1ПМ ≈ ${kotlin.math.round(estimated1Rm).toInt()} кг). Адаптируем рабочий вес до $finalWeight кг для высокой метаболической плотности.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
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
                            goalEmoji = goal.emoji, goal = goal
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
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    else -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Похудение и рельеф. Главная задача на дефиците — удерживать силовые показатели ($lastWeight кг) и сохранять сухую мышечную массу.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                }
            }

            FitnessGoal.HEALTH_FITNESS -> {
                when {
                    lastReps >= maxReps + 3 && !reachedFailure -> {
                        val estimated1Rm = estimate1Rm(lastWeight, lastReps)
                        val targetMidpoint = (minReps + maxReps) / 2
                        val calculatedWeight = estimateTargetWeightForReps(estimated1Rm, targetMidpoint)
                        val snappedTarget = equipmentProfile.snapWeight(calculatedWeight)
                        val finalWeight = maxOf(snappedTarget, equipmentProfile.snapWeight(lastWeight + step))
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Здоровье и тонус. Выполнено $lastReps повт. с весом $lastWeight кг. Вес слишком легкий. Мягко повышаем нагрузку до $finalWeight кг для тонуса мышц.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    allHitMaxReps && (allEffortEasy || workingSets.all { it.effort == PerceivedEffort.OPTIMAL }) -> {
                        val nextWeight = equipmentProfile.snapWeight(lastWeight + step)
                        val finalWeight = if (nextWeight <= lastWeight) lastWeight + step else nextWeight
                        ProgressionRecommendation(
                            suggestedWeight = finalWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.INCREASE_WEIGHT,
                            rationaleRu = "Цель: Здоровье и тонус. Организм отлично справляется. Мягко повышаем нагрузку до $finalWeight кг для укрепления мышечного корсета.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
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
                            goalEmoji = goal.emoji, goal = goal
                        )
                    }
                    else -> {
                        ProgressionRecommendation(
                            suggestedWeight = lastWeight,
                            suggestedReps = "$minReps-$maxReps",
                            decision = ProgressionDecision.MAINTAIN_WEIGHT,
                            rationaleRu = "Цель: Здоровье и тонус. Оставьте $lastWeight кг. Фокусируйтесь на ровном дыхании и идеальной траектории движения.",
                            goalTitle = goal.displayNameRu,
                            goalEmoji = goal.emoji, goal = goal
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
            val rawStarting = when (equipmentProfile.equipment) {
                Equipment.DUMBBELL -> 8.0
                Equipment.BARBELL -> equipmentProfile.barWeight
                Equipment.CABLE -> 12.5
                Equipment.MACHINE -> 15.0
                Equipment.BODYWEIGHT -> 0.0
                else -> 10.0
            }
            val baseWeight = equipmentProfile.snapWeight(rawStarting)
            return ProgressionRecommendation(
                suggestedWeight = baseWeight,
                suggestedReps = "$targetMinReps-$targetMaxReps",
                decision = ProgressionDecision.MAINTAIN_WEIGHT,
                rationaleRu = "Первая тренировка: начните с базового веса $baseWeight кг и отработайте технику."
            )
        }

        val lastWeight = workingSets.last().weight
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
