package com.gym.feature.templates.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.database.mapper.toEntity
import com.gym.core.model.FitnessGoal
import com.gym.core.model.Gender
import com.gym.core.model.TrainingExperience
import com.gym.core.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Количество шагов мастера онбординга */
const val TOTAL_STEPS = 3

/**
 * UI-состояние экрана онбординга (MVI State).
 */
data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val gender: Gender = Gender.MALE,
    val age: Int = 25,
    val heightCm: Float = 175f,
    val weightKg: Float = 75f,
    val experience: TrainingExperience = TrainingExperience.BEGINNER,
    val primaryGoal: FitnessGoal = FitnessGoal.HYPERTROPHY,
    val workoutsPerWeek: Int = 3,
    val bmi: Float = 0f,
    val recommendedProgram: String = "",
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
)

/**
 * Интенты пользователя для экрана онбординга (MVI Intent).
 */
sealed interface OnboardingIntent {
    data class SetName(val name: String) : OnboardingIntent
    data class SetGender(val gender: Gender) : OnboardingIntent
    data class SetAge(val age: Int) : OnboardingIntent
    data class SetHeight(val heightCm: Float) : OnboardingIntent
    data class SetWeight(val weightKg: Float) : OnboardingIntent
    data class SetExperience(val experience: TrainingExperience) : OnboardingIntent
    data class SetGoal(val goal: FitnessGoal) : OnboardingIntent
    data class SetWorkoutsPerWeek(val count: Int) : OnboardingIntent
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent
    data object CompleteOnboarding : OnboardingIntent
}

/**
 * ViewModel мастера онбординга.
 *
 * Управляет трёхшаговым визардом первичной настройки профиля пользователя.
 * После завершения сохраняет [UserProfile] в Room через [UserProfileDao].
 */
class OnboardingViewModel(
    private val userProfileDao: UserProfileDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState().let { initial ->
            initial.copy(
                bmi = calculateBmi(initial.heightCm, initial.weightKg),
                recommendedProgram = recommendProgram(
                    initial.experience,
                    initial.primaryGoal,
                ),
            )
        }
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = userProfileDao.getUserProfileSync()
            if (existing != null) {
                _uiState.update { current ->
                    current.copy(
                        name = existing.name,
                        gender = existing.gender,
                        age = existing.age,
                        heightCm = existing.heightCm,
                        weightKg = existing.weightKg,
                        experience = existing.experience,
                        primaryGoal = existing.primaryGoal,
                        workoutsPerWeek = existing.workoutsPerWeek,
                        bmi = calculateBmi(existing.heightCm, existing.weightKg),
                        recommendedProgram = recommendProgram(existing.experience, existing.primaryGoal)
                    )
                }
            }
        }
    }

    /** Единая точка входа для обработки интентов (MVI) */
    fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.SetName -> updateState { copy(name = intent.name) }

            is OnboardingIntent.SetGender -> updateState { copy(gender = intent.gender) }

            is OnboardingIntent.SetAge -> updateState { copy(age = intent.age) }

            is OnboardingIntent.SetHeight -> updateStateWithRecalc { copy(heightCm = intent.heightCm) }

            is OnboardingIntent.SetWeight -> updateStateWithRecalc { copy(weightKg = intent.weightKg) }

            is OnboardingIntent.SetExperience -> updateStateWithRecalc {
                copy(experience = intent.experience)
            }

            is OnboardingIntent.SetGoal -> updateStateWithRecalc {
                copy(primaryGoal = intent.goal)
            }

            is OnboardingIntent.SetWorkoutsPerWeek -> updateState {
                copy(workoutsPerWeek = intent.count)
            }

            OnboardingIntent.NextStep -> handleNextStep()
            OnboardingIntent.PreviousStep -> handlePreviousStep()
            OnboardingIntent.CompleteOnboarding -> completeOnboarding()
        }
    }

    // ── Навигация по шагам ──────────────────────────────────────────

    private fun handleNextStep() {
        val state = _uiState.value
        // Валидация: на шаге 0 имя обязательно
        if (state.currentStep == 0 && state.name.isBlank()) return
        if (state.currentStep < TOTAL_STEPS - 1) {
            updateState { copy(currentStep = currentStep + 1) }
        }
    }

    private fun handlePreviousStep() {
        if (_uiState.value.currentStep > 0) {
            updateState { copy(currentStep = currentStep - 1) }
        }
    }

    // ── Завершение онбординга ───────────────────────────────────────

    private fun completeOnboarding() {
        if (_uiState.value.isSaving) return
        updateState { copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val profile = UserProfile(
                id = "default_user",
                name = state.name,
                gender = state.gender,
                age = state.age,
                heightCm = state.heightCm,
                weightKg = state.weightKg,
                experience = state.experience,
                primaryGoal = state.primaryGoal,
                workoutsPerWeek = state.workoutsPerWeek,
                isCompleted = true,
            )
            userProfileDao.saveUserProfile(profile.toEntity())
            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
        }
    }

    // ── Вспомогательные функции ─────────────────────────────────────

    /** Обновить state и пересчитать BMI + рекомендованную программу */
    private inline fun updateStateWithRecalc(crossinline transform: OnboardingUiState.() -> OnboardingUiState) {
        _uiState.update { current ->
            val updated = current.transform()
            updated.copy(
                bmi = calculateBmi(updated.heightCm, updated.weightKg),
                recommendedProgram = recommendProgram(updated.experience, updated.primaryGoal),
            )
        }
    }

    /** Простое обновление state без пересчёта */
    private inline fun updateState(crossinline transform: OnboardingUiState.() -> OnboardingUiState) {
        _uiState.update { it.transform() }
    }

    companion object {

        /** Расчёт индекса массы тела: вес (кг) / рост (м)² */
        fun calculateBmi(heightCm: Float, weightKg: Float): Float {
            if (heightCm <= 0f) return 0f
            val heightM = heightCm / 100f
            return weightKg / (heightM * heightM)
        }

        /**
         * Подбор рекомендованной программы по опыту и цели.
         * Соответствует логике [UserProfile.recommendedProgramTitle].
         */
        fun recommendProgram(
            experience: TrainingExperience,
            goal: FitnessGoal,
        ): String = when (experience) {
            TrainingExperience.BEGINNER -> when (goal) {
                FitnessGoal.HYPERTROPHY -> "Full Body 3×/нед"
                FitnessGoal.STRENGTH -> "Starting Strength"
                FitnessGoal.FAT_LOSS -> "Full Body + Кардио"
                FitnessGoal.HEALTH_FITNESS -> "Full Body Лайт"
            }
            TrainingExperience.INTERMEDIATE -> when (goal) {
                FitnessGoal.HYPERTROPHY -> "Push / Pull / Legs"
                FitnessGoal.STRENGTH -> "5/3/1 Wendler"
                FitnessGoal.FAT_LOSS -> "Upper / Lower + HIIT"
                FitnessGoal.HEALTH_FITNESS -> "Upper / Lower 3×/нед"
            }
            TrainingExperience.ADVANCED -> when (goal) {
                FitnessGoal.HYPERTROPHY -> "PPL 6×/нед"
                FitnessGoal.STRENGTH -> "Conjugate / Westside"
                FitnessGoal.FAT_LOSS -> "Push Pull Legs + Кардио"
                FitnessGoal.HEALTH_FITNESS -> "Full Body 4×/нед"
            }
        }
    }
}
