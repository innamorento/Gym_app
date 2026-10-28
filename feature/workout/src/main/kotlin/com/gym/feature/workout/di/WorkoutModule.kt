package com.gym.feature.workout.di

import com.gym.feature.workout.WorkoutViewModel
import com.gym.feature.workout.analytics.AnalyticsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val workoutModule = module {
    viewModel { WorkoutViewModel(get(), get(), get(), androidContext()) }
    viewModel { AnalyticsViewModel(get(), get()) }
}
