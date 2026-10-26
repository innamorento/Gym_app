package com.gym.core.model

enum class MuscleGroup(val displayNameRu: String) {
    CHEST("Грудь"),
    BACK("Спина"),
    LEGS_QUADS("Квадрицепсы"),
    LEGS_HAMSTRINGS("Бицепс бедра"),
    CALVES("Икры"),
    SHOULDERS("Плечи"),
    BICEPS("Бицепс"),
    TRICEPS("Трицепс"),
    CORE("Пресс / Кор"),
    FULL_BODY("Все тело")
}

enum class Equipment(val displayNameRu: String) {
    BARBELL("Штанга"),
    DUMBBELL("Гантели"),
    MACHINE("Тренажер"),
    CABLE("Блочный тренажер"),
    BODYWEIGHT("Собственный вес"),
    WEIGHTED_BODYWEIGHT("С отягощением"),
    KETTLEBELL("Гиря"),
    RESISTANCE_BAND("Резина / Эспандер"),
    OTHER("Другое")
}

enum class SetType(val shortCode: String, val displayNameRu: String) {
    WARMUP("W", "Разминка"),
    NORMAL("", "Рабочий"),
    DROP("D", "Дропсет"),
    FAILURE("F", "До отказа")
}

enum class WorkoutStatus {
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class WeightUnit(val symbol: String) {
    KG("кг"),
    LBS("lbs")
}

/**
 * Быстрая шкала субъективного усилия атлета (RPE / RIR).
 */
enum class PerceivedEffort(
    val shortLabel: String,
    val descriptionRu: String,
    val approximateRir: String,
    val approximateRpe: Float
) {
    EASY("Легко", "Запас сил 3+ повторения", "3+ повт.", 7.0f),
    OPTIMAL("В цель", "Идеальный рабочий стимул (запас ~2 повт.)", "2 повт.", 8.0f),
    HARD("Тяжело", "Близко к отказу (запас 1 повт.)", "1 повт.", 9.0f),
    FAILURE("Отказ", "Предельное усилие / последнее повторение из последних сил", "0 повт.", 10.0f)
}

/**
 * 4 макро-хаба анатомических групп мышц для ликвидации бесконечного скролла.
 */
enum class MuscleRegion(
    val displayNameRu: String,
    val emoji: String,
    val muscleGroups: List<MuscleGroup>
) {
    PUSH(
        displayNameRu = "Жимовые (Push)",
        emoji = "🏋️‍♂️",
        muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
    ),
    PULL(
        displayNameRu = "Тяговые (Pull)",
        emoji = "🦾",
        muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS)
    ),
    LEGS(
        displayNameRu = "Низ тела (Legs)",
        emoji = "🦵",
        muscleGroups = listOf(MuscleGroup.LEGS_QUADS, MuscleGroup.LEGS_HAMSTRINGS, MuscleGroup.CALVES)
    ),
    CORE(
        displayNameRu = "Кор и стабилизаторы (Core)",
        emoji = "🛡️",
        muscleGroups = listOf(MuscleGroup.CORE, MuscleGroup.FULL_BODY)
    );

    companion object {
        fun regionFor(group: MuscleGroup): MuscleRegion {
            return values().firstOrNull { it.muscleGroups.contains(group) } ?: PUSH
        }
    }
}
