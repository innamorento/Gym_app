package com.gym.core.model

enum class Gender(val displayNameRu: String) {
    MALE("Мужской"),
    FEMALE("Женский")
}

enum class TrainingExperience(val displayNameRu: String, val subtitleRu: String) {
    BEGINNER("Новичок", "Занимаюсь менее 6 месяцев, изучаю технику"),
    INTERMEDIATE("Средний", "Стаж от 6 месяцев до 2 лет, знаю базу"),
    ADVANCED("Опытный", "Стаж более 2 лет, работаю по периодизации")
}

enum class FitnessGoal(val displayNameRu: String, val emoji: String, val descriptionRu: String) {
    HYPERTROPHY("Набор массы", "💪", "Максимальный рост мышц и объемов тела"),
    STRENGTH("Развитие силы", "🏋️", "Увеличение рабочего веса в базовых движениях"),
    FAT_LOSS("Похудение и рельеф", "🔥", "Сжигание жира и сохранение мышечной плотности"),
    HEALTH_FITNESS("Здоровье и тонус", "⚡", "Поддержание отличного самочувствия и энергии")
}

data class UserProfile(
    val id: String = "default_user",
    val name: String = "",
    val gender: Gender = Gender.MALE,
    val age: Int = 25,
    val heightCm: Float = 175f,
    val weightKg: Float = 75f,
    val experience: TrainingExperience = TrainingExperience.BEGINNER,
    val primaryGoal: FitnessGoal = FitnessGoal.HYPERTROPHY,
    val workoutsPerWeek: Int = 3,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Индекс массы тела (BMI)
     */
    val bmi: Float
        get() {
            val heightM = heightCm / 100f
            return if (heightM > 0f) weightKg / (heightM * heightM) else 0f
        }

    /**
     * Рекомендация стартовой программы на основе профиля
     */
    val recommendedProgramTitle: String
        get() = when {
            experience == TrainingExperience.BEGINNER -> "Фулбоди (Full Body)"
            workoutsPerWeek <= 3 -> "Фулбоди (Full Body)"
            primaryGoal == FitnessGoal.HYPERTROPHY -> "Грудь + Трицепс (Push)"
            primaryGoal == FitnessGoal.STRENGTH -> "Спина + Бицепс (Pull)"
            else -> "Ноги + Плечи (Legs & Shoulders)"
        }
}
