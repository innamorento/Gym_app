package com.gym.feature.templates.di

import com.gym.feature.templates.TemplatesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val templatesModule = module {
    viewModel { TemplatesViewModel(get()) }
}
