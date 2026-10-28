package com.gym.feature.workout.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.gym.feature.workout.RestTimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RestTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var countdownJob: Job? = null

    companion object {
        const val ACTION_START = "com.gym.feature.workout.service.ACTION_START"
        const val ACTION_ADJUST = "com.gym.feature.workout.service.ACTION_ADJUST"
        const val ACTION_STOP = "com.gym.feature.workout.service.ACTION_STOP"
        const val EXTRA_SECONDS = "extra_seconds"
        const val EXTRA_DELTA_SECONDS = "extra_delta_seconds"

        private const val CHANNEL_ID = "gym_rest_timer_channel"
        private const val NOTIFICATION_ID = 1001

        private val _serviceTimerState = MutableStateFlow(RestTimerState())
        val serviceTimerState: StateFlow<RestTimerState> = _serviceTimerState.asStateFlow()

        fun start(context: Context, seconds: Int) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SECONDS, seconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun adjust(context: Context, deltaSeconds: Int) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_ADJUST
                putExtra(EXTRA_DELTA_SECONDS, deltaSeconds)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 90).coerceAtLeast(1)
                startCountdown(seconds)
            }
            ACTION_ADJUST -> {
                val delta = intent.getIntExtra(EXTRA_DELTA_SECONDS, 30)
                adjustCountdown(delta)
            }
            ACTION_STOP -> {
                stopCountdown()
            }
        }
        return START_NOT_STICKY
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        _serviceTimerState.value = RestTimerState(
            isRunning = true,
            totalSeconds = seconds,
            remainingSeconds = seconds
        )

        // Запуск переднего плана с первым уведомлением
        val initialNotification = buildNotification(seconds, seconds)
        startForeground(NOTIFICATION_ID, initialNotification)

        countdownJob = serviceScope.launch {
            while (isActive && _serviceTimerState.value.remainingSeconds > 0) {
                delay(1000)
                _serviceTimerState.update { current ->
                    if (current.remainingSeconds > 1) {
                        current.copy(remainingSeconds = current.remainingSeconds - 1)
                    } else {
                        current.copy(isRunning = false, remainingSeconds = 0)
                    }
                }

                val current = _serviceTimerState.value
                if (current.remainingSeconds > 0) {
                    updateNotification(current.remainingSeconds, current.totalSeconds)
                } else {
                    onTimerFinished()
                    break
                }
            }
        }
    }

    private fun adjustCountdown(delta: Int) {
        _serviceTimerState.update { current ->
            val newRemaining = (current.remainingSeconds + delta).coerceAtLeast(0)
            val newTotal = (current.totalSeconds + delta).coerceAtLeast(newRemaining)
            current.copy(
                totalSeconds = newTotal,
                remainingSeconds = newRemaining,
                isRunning = newRemaining > 0
            )
        }
        val current = _serviceTimerState.value
        if (current.isRunning) {
            updateNotification(current.remainingSeconds, current.totalSeconds)
            if (countdownJob?.isActive != true) {
                startCountdown(current.remainingSeconds)
            }
        } else {
            stopCountdown()
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        _serviceTimerState.value = RestTimerState(isRunning = false, remainingSeconds = 0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerFinished() {
        // Тройной отчетливый вибросигнал окончания отдыха
        triggerVibration()

        // Обновляем уведомление о готовности к новому подходу
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val finishedNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("✅ Отдых завершен!")
            .setContentText("Время выходить на следующий подход! Вперед!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, finishedNotification)

        serviceScope.launch {
            delay(4000)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun triggerVibration() {
        try {
            val pattern = longArrayOf(0, 350, 150, 350, 150, 600)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            // Игнорируем если устройство не поддерживает вибрацию
        }
    }

    private fun buildNotification(remaining: Int, total: Int): android.app.Notification {
        val mins = remaining / 60
        val secs = remaining % 60
        val timeFormatted = String.format("%02d:%02d", mins, secs)

        // PendingIntent для нажатия на уведомление (открывает приложение)
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка: +30 секунд
        val adjustIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_ADJUST
            putExtra(EXTRA_DELTA_SECONDS, 30)
        }
        val adjustPendingIntent = PendingIntent.getService(
            this,
            1,
            adjustIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка: Пропустить отдых
        val stopIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏳ Отдых: $timeFormatted")
            .setContentText("Следующий подход на подходе. Не отвлекайтесь!")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total, remaining, false)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_input_add, "+30с", adjustPendingIntent)
            .addAction(android.R.drawable.ic_delete, "Пропустить", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(remaining: Int, total: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(remaining, total))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Таймер отдыха в зале",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отображает обратный отсчет отдыха между подходами"
                enableVibration(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
