package com.gym.feature.exercises.di

import com.gym.feature.exercises.ExercisesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val exercisesModule = module {
    viewModel { ExercisesViewModel(get()) }
}
