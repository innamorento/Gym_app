package com.gym.core.model

/**
 * День в недельной тренировочной программе.
 * [dayOfWeek] - день недели от 1 (Понедельник) до 7 (Воскресенье).
 */
data class WorkoutPlanDay(
    val id: Long = 0,
    val planId: Long = 0,
    val dayOfWeek: Int, // 1 (Mon) .. 7 (Sun)
    val dayNameRu: String,
    val templateId: Long? = null,
    val templateTitle: String? = null,
    val isRestDay: Boolean = false,
    val focusDescription: String? = null
) {
    val dayTitleRu: String
        get() = when (dayOfWeek) {
            1 -> "Понедельник"
            2 -> "Вторник"
            3 -> "Среда"
            4 -> "Четверг"
            5 -> "Пятница"
            6 -> "Суббота"
            7 -> "Воскресенье"
            else -> "День $dayOfWeek"
        }

    val dayShortRu: String
        get() = when (dayOfWeek) {
            1 -> "Пн"
            2 -> "Вт"
            3 -> "Ср"
            4 -> "Чт"
            5 -> "Пт"
            6 -> "Сб"
            7 -> "Вс"
            else -> "Д$dayOfWeek"
        }
}

/**
 * Недельная тренировочная программа / Сплит (Workout Routine / Split).
 * Позволяет спортсмену заниматься по проверенной недельной системе месяцами без необходимости
 * каждый раз вспоминать и выбирать упражнения перед походом в зал.
 */
data class WorkoutPlan(
    val id: Long = 0,
    val title: String,
    val description: String,
    val level: TrainingExperience = TrainingExperience.INTERMEDIATE,
    val daysPerWeek: Int = 3,
    val isActive: Boolean = false,
    val days: List<WorkoutPlanDay> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
