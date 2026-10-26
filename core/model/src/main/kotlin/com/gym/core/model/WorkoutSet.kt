package com.gym.core.model

import kotlin.math.roundToInt

data class WorkoutSet(
    val id: Long = 0,
    val setNumber: Int,
    val setType: SetType = SetType.NORMAL,
    val weight: Double,
    val reps: Int,
    val rpe: Float? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val restTimeSeconds: Int? = null
) {
    /**
     * Расчет объема подхода (Volume).
     * Разминочные подходы (WARMUP) не учитываются в рабочем объёме.
     */
    val volume: Double
        get() = if (setType == SetType.WARMUP) 0.0 else weight * reps

    /**
     * Расчет одноповторного максимума (Estimated 1RM) по формуле Epley.
     * 1RM = Weight * (1 + Reps / 30)
     */
    val estimated1RM: Double
        get() {
            if (reps <= 0 || weight <= 0.0) return 0.0
            if (reps == 1) return weight
            val result = weight * (1.0 + reps / 30.0)
            return (result * 10.0).roundToInt() / 10.0
        }
}
