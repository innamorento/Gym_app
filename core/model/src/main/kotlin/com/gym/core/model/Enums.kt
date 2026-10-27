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
 * Понятные анатомические категории мышц (взамен абстрактных Push/Pull):
 * Грудь, Спина, Ноги, Плечи, Бицепс, Трицепс, Руки, Пресс, Прочее.
 */
enum class MuscleCategory(
    val displayNameRu: String,
    val emoji: String,
    val muscleGroups: List<MuscleGroup>
) {
    CHEST("Грудь", "🥋", listOf(MuscleGroup.CHEST)),
    BACK("Спина", "🦅", listOf(MuscleGroup.BACK)),
    LEGS("Ноги", "🦵", listOf(MuscleGroup.LEGS_QUADS, MuscleGroup.LEGS_HAMSTRINGS, MuscleGroup.CALVES)),
    SHOULDERS("Плечи", "🎯", listOf(MuscleGroup.SHOULDERS)),
    BICEPS("Бицепс", "💪", listOf(MuscleGroup.BICEPS)),
    TRICEPS("Трицепс", "⚡", listOf(MuscleGroup.TRICEPS)),
    ARMS("Руки", "🦾", listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS)),
    CORE("Пресс / Кор", "🛡️", listOf(MuscleGroup.CORE)),
    OTHER("Прочее", "🌐", listOf(MuscleGroup.FULL_BODY));

    companion object {
        fun categoryFor(group: MuscleGroup): MuscleCategory {
            return when (group) {
                MuscleGroup.CHEST -> CHEST
                MuscleGroup.BACK -> BACK
                MuscleGroup.LEGS_QUADS, MuscleGroup.LEGS_HAMSTRINGS, MuscleGroup.CALVES -> LEGS
                MuscleGroup.SHOULDERS -> SHOULDERS
                MuscleGroup.BICEPS -> BICEPS
                MuscleGroup.TRICEPS -> TRICEPS
                MuscleGroup.CORE -> CORE
                MuscleGroup.FULL_BODY -> OTHER
            }
        }
    }
}

/**
 * Обратная совместимость для MuscleRegion
 */
typealias MuscleRegion = MuscleCategory
