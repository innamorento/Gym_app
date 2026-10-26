package com.gym.tracker

import android.app.Application
import com.gym.core.database.di.databaseModule
import com.gym.feature.exercises.di.exercisesModule
import com.gym.feature.templates.di.templatesModule
import com.gym.feature.workout.di.workoutModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class GymApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@GymApp)
            modules(
                databaseModule,
                templatesModule,
                exercisesModule,
                workoutModule
            )
        }
    }
}
