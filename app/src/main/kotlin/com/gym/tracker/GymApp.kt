package com.gym.tracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gym.core.database.di.databaseModule
import com.gym.feature.exercises.di.exercisesModule
import com.gym.feature.templates.di.templatesModule
import com.gym.feature.workout.di.workoutModule
import com.gym.tracker.reminder.WorkoutReminderWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.concurrent.TimeUnit

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

        scheduleWorkoutReminders()
    }

    private fun scheduleWorkoutReminders() {
        val reminderRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WorkoutReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}
