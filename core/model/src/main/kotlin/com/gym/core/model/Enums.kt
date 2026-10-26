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
