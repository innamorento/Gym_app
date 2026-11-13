package com.gym.tracker.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gym.core.database.dao.WorkoutDao
import com.gym.tracker.MainActivity
import com.gym.tracker.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WorkoutReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val workoutDao: WorkoutDao by inject()

    override suspend fun doWork(): Result {
        val latest = workoutDao.getLatestCompletedWorkout()
        val now = System.currentTimeMillis()
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L

        // Если с момента последней тренировки прошло 3 или более дней (или тренировок еще не было)
        if (latest == null || (now - (latest.endTime ?: latest.startTime)) >= threeDaysMs) {
            sendReminderNotification()
        }

        return Result.success()
    }

    private fun sendReminderNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "workout_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Напоминания о тренировках",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Мотивационные напоминания, если вы давно не тренировались"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Время потренироваться! 💪")
            .setContentText("Прошло уже 3 дня с последней тренировки. Железо ждет в зале!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "periodic_workout_reminder"
    }
}
