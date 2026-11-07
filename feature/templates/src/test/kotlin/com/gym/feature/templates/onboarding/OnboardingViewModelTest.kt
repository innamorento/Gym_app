package com.gym.feature.templates.onboarding

import com.gym.core.database.dao.UserProfileDao
import com.gym.core.database.entity.UserProfileEntity
import com.gym.core.model.FitnessGoal
import com.gym.core.model.Gender
import com.gym.core.model.TrainingExperience
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OnboardingViewModelTest {

    private lateinit var fakeDao: FakeUserProfileDao
    private lateinit var viewModel: OnboardingViewModel

    class FakeUserProfileDao : UserProfileDao {
        var savedProfile: UserProfileEntity? = null

        override fun getUserProfile(id: String): Flow<UserProfileEntity?> = flowOf(savedProfile)

        override suspend fun getUserProfileSync(id: String): UserProfileEntity? = savedProfile

        override suspend fun saveUserProfile(profile: UserProfileEntity) {
            savedProfile = profile
        }

        override suspend fun updateWeight(newWeight: Float, id: String) {
            savedProfile = savedProfile?.copy(weightKg = newWeight)
        }

        override suspend fun hasCompletedProfile(): Int =
            if (savedProfile?.isCompleted == true) 1 else 0
    }

    @Before
    fun setUp() {
        fakeDao = FakeUserProfileDao()
        viewModel = OnboardingViewModel(fakeDao)
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertEquals(0, state.currentStep)
        assertEquals("", state.name)
        assertEquals(Gender.MALE, state.gender)
        assertEquals(25, state.age)
        assertEquals(175f, state.heightCm, 0.01f)
        assertEquals(75f, state.weightKg, 0.01f)
        assertEquals(TrainingExperience.BEGINNER, state.experience)
        assertEquals(FitnessGoal.HYPERTROPHY, state.primaryGoal)
        assertEquals(3, state.workoutsPerWeek)
        assertTrue(state.bmi > 0f)
        assertTrue(state.recommendedProgram.isNotBlank())
        assertFalse(state.isCompleted)
    }

    @Test
    fun testStep0ValidationNameCannotBeBlank() {
        // Имя пустое - переход на шаг 1 заблокирован
        viewModel.handleIntent(OnboardingIntent.NextStep)
        assertEquals(0, viewModel.uiState.value.currentStep)

        // Ввели только пробелы - все еще заблокирован
        viewModel.handleIntent(OnboardingIntent.SetName("   "))
        viewModel.handleIntent(OnboardingIntent.NextStep)
        assertEquals(0, viewModel.uiState.value.currentStep)

        // Ввели нормальное имя - переход разрешен
        viewModel.handleIntent(OnboardingIntent.SetName("Михаил"))
        viewModel.handleIntent(OnboardingIntent.NextStep)
        assertEquals(1, viewModel.uiState.value.currentStep)
    }

    @Test
    fun testAnthropometryUpdatesRecalculateBmi() {
        viewModel.handleIntent(OnboardingIntent.SetHeight(180f))
        viewModel.handleIntent(OnboardingIntent.SetWeight(81f))

        val state = viewModel.uiState.value
        // 81 / (1.8 * 1.8) = 25.0
        assertEquals(25.0f, state.bmi, 0.05f)
    }

    @Test
    fun testStepNavigationLimits() {
        // Нельзя уйти назад ниже 0
        viewModel.handleIntent(OnboardingIntent.PreviousStep)
        assertEquals(0, viewModel.uiState.value.currentStep)

        // Заполняем имя и идем вперед
        viewModel.handleIntent(OnboardingIntent.SetName("Алексей"))
        viewModel.handleIntent(OnboardingIntent.NextStep) // шаг 1
        assertEquals(1, viewModel.uiState.value.currentStep)

        viewModel.handleIntent(OnboardingIntent.NextStep) // шаг 2 (последний)
        assertEquals(2, viewModel.uiState.value.currentStep)

        // Нельзя уйти вперед дальше шага 2
        viewModel.handleIntent(OnboardingIntent.NextStep)
        assertEquals(2, viewModel.uiState.value.currentStep)

        // Идем назад на шаг 1
        viewModel.handleIntent(OnboardingIntent.PreviousStep)
        assertEquals(1, viewModel.uiState.value.currentStep)
    }

    @Test
    fun testRecommendProgramHelper() {
        assertEquals("Full Body 3×/нед", OnboardingViewModel.recommendProgram(TrainingExperience.BEGINNER, FitnessGoal.HYPERTROPHY))
        assertEquals("Starting Strength", OnboardingViewModel.recommendProgram(TrainingExperience.BEGINNER, FitnessGoal.STRENGTH))
        assertEquals("Push / Pull / Legs", OnboardingViewModel.recommendProgram(TrainingExperience.INTERMEDIATE, FitnessGoal.HYPERTROPHY))
        assertEquals("5/3/1 Wendler", OnboardingViewModel.recommendProgram(TrainingExperience.INTERMEDIATE, FitnessGoal.STRENGTH))
        assertEquals("PPL 6×/нед", OnboardingViewModel.recommendProgram(TrainingExperience.ADVANCED, FitnessGoal.HYPERTROPHY))
    }

    @Test
    fun testBmiCalculationHelper() {
        assertEquals(0f, OnboardingViewModel.calculateBmi(0f, 75f), 0.001f)
        assertEquals(24.69f, OnboardingViewModel.calculateBmi(180f, 80f), 0.05f)
    }
}
