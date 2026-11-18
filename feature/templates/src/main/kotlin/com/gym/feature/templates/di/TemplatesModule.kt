package com.gym.feature.templates.di

import com.gym.feature.templates.TemplatesViewModel
import com.gym.feature.templates.auth.AuthViewModel
import com.gym.feature.templates.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val templatesModule = module {
    viewModel { TemplatesViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { AuthViewModel(get()) }
}
