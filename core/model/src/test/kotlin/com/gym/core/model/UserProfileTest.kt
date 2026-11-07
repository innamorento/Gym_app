package com.gym.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileTest {

    @Test
    fun testBmiCalculationNormal() {
        val profile = UserProfile(
            heightCm = 180f,
            weightKg = 81f
        )
        // 81 / (1.8 * 1.8) = 81 / 3.24 = 25.0
        assertEquals(25.0f, profile.bmi, 0.05f)
    }

    @Test
    fun testBmiCalculationZeroHeight() {
        val profile = UserProfile(
            heightCm = 0f,
            weightKg = 75f
        )
        assertEquals(0f, profile.bmi, 0.001f)
    }

    @Test
    fun testRecommendedProgramForBeginner() {
        val profile = UserProfile(
            experience = TrainingExperience.BEGINNER,
            primaryGoal = FitnessGoal.STRENGTH,
            workoutsPerWeek = 4
        )
        // Новички всегда направляются на Фулбоди независимо от цели
        assertEquals("Фулбоди (Full Body)", profile.recommendedProgramTitle)
    }

    @Test
    fun testRecommendedProgramForLowFrequency() {
        val profile = UserProfile(
            experience = TrainingExperience.INTERMEDIATE,
            primaryGoal = FitnessGoal.HYPERTROPHY,
            workoutsPerWeek = 3
        )
        // При 3 или менее тренировках в неделю рекомендуется Фулбоди
        assertEquals("Фулбоди (Full Body)", profile.recommendedProgramTitle)
    }

    @Test
    fun testRecommendedProgramForHypertrophySplit() {
        val profile = UserProfile(
            experience = TrainingExperience.INTERMEDIATE,
            primaryGoal = FitnessGoal.HYPERTROPHY,
            workoutsPerWeek = 4
        )
        assertEquals("Грудь + Трицепс (Push)", profile.recommendedProgramTitle)
    }

    @Test
    fun testRecommendedProgramForStrengthSplit() {
        val profile = UserProfile(
            experience = TrainingExperience.INTERMEDIATE,
            primaryGoal = FitnessGoal.STRENGTH,
            workoutsPerWeek = 4
        )
        assertEquals("Спина + Бицепс (Pull)", profile.recommendedProgramTitle)
    }

    @Test
    fun testRecommendedProgramForFatLossSplit() {
        val profile = UserProfile(
            experience = TrainingExperience.INTERMEDIATE,
            primaryGoal = FitnessGoal.FAT_LOSS,
            workoutsPerWeek = 5
        )
        assertEquals("Ноги + Плечи (Legs & Shoulders)", profile.recommendedProgramTitle)
    }
}
