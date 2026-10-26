package com.gym.feature.workout.di

import com.gym.feature.workout.WorkoutViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val workoutModule = module {
    viewModel { WorkoutViewModel(get(), get(), get()) }
}
