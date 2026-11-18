package com.gym.feature.workout.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.Workout
import com.gym.core.ui.icon.GymIcons
import androidx.compose.ui.res.painterResource
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymSurfaceBorder
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import com.gym.feature.workout.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Экран праздничной сводки завершенной тренировки (Workout Summary).
 * Показывает суммарный тоннаж, время, количество подходов и возможность поделиться результатом.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    workoutId: Long,
    viewModel: WorkoutViewModel,
    onDoneClick: () -> Unit
) {
    val context = LocalContext.current
    var workout by remember { mutableStateOf<Workout?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(workoutId) {
        workout = viewModel.getWorkoutDetails(workoutId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Сводка тренировки",
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymDarkBackground)
            )
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GymAccentGreen)
            }
            return@Scaffold
        }

        val currentWorkout = workout
        if (currentWorkout == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Тренировка не найдена", color = GymTextSecondary)
            }
            return@Scaffold
        }

        val durationMinutes = (currentWorkout.durationSeconds ?: 0L) / 60
        val durationHours = durationMinutes / 60
        val remMins = durationMinutes % 60
        val durationText = if (durationHours > 0) "${durationHours}ч ${remMins}м" else "${remMins}м"

        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
        val dateString = dateFormat.format(Date(currentWorkout.startTime))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Праздничный заголовок
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymAccentGreen.copy(alpha = 0.5f))),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(GymAccentGreen.copy(alpha = 0.15f))
                                .border(1.dp, GymAccentGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GymAccentGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Отличная работа! 🎉",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymTextPrimary
                        )

                        Text(
                            text = currentWorkout.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GymAccentGreen,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = dateString,
                            fontSize = 12.sp,
                            color = GymTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // 3 Главные метрики: Время, Тоннаж, Подходы
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Время
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, GymSurfaceBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = GymAccentBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Время", fontSize = 11.sp, color = GymTextSecondary)
                            Text(
                                text = durationText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymTextPrimary,
                                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                            )
                        }
                    }

                    // Тоннаж
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, GymSurfaceBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = GymAccentGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Тоннаж", fontSize = 11.sp, color = GymTextSecondary)
                            Text(
                                text = "${currentWorkout.totalVolume.toInt()} кг",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymTextPrimary,
                                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                            )
                        }
                    }

                    // Подходы
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, GymSurfaceBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GymAccentBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Сеты", fontSize = 11.sp, color = GymTextSecondary)
                            Text(
                                text = "${currentWorkout.totalCompletedSets}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymTextPrimary,
                                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                            )
                        }
                    }
                }
            }

            // Заголовок выполненных упражнений
            item {
                Text(
                    text = "Выполненные упражнения (${currentWorkout.exercises.size}):",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
            }

            // Список упражнений и их результатов
            items(currentWorkout.exercises) { exercise ->
                val completedSets = exercise.sets.filter { it.isCompleted }
                val maxWeight = completedSets.maxOfOrNull { it.weight } ?: 0.0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GymSurfaceBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercise.exercise.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${completedSets.size} сетов",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymAccentGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val setsSummary = completedSets.joinToString(separator = ", ") { set ->
                            "${set.weight}x${set.reps}"
                        }
                        Text(
                            text = if (setsSummary.isNotBlank()) setsSummary else "Нет завершенных подходов",
                            fontSize = 12.sp,
                            color = GymTextSecondary,
                            style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                        )

                        if (maxWeight > 0.0) {
                            Text(
                                text = "Максимальный вес: ${maxWeight} кг",
                                fontSize = 11.sp,
                                color = GymAccentBlue,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Кнопки действий: Поделиться и Закрыть
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = buildString {
                                appendLine("🏋️ Gym Workout Tracker")
                                appendLine("Тренировка: ${currentWorkout.title}")
                                appendLine("⏱ Время: $durationText")
                                appendLine("⚡ Тоннаж: ${currentWorkout.totalVolume.toInt()} кг")
                                appendLine("💪 Подходов: ${currentWorkout.totalCompletedSets}")
                                appendLine()
                                appendLine("Упражнения:")
                                currentWorkout.exercises.forEach { ex ->
                                    val sets = ex.sets.filter { it.isCompleted }
                                    if (sets.isNotEmpty()) {
                                        val summary = sets.joinToString(", ") { "${it.weight}x${it.reps}" }
                                        appendLine("• ${ex.exercise.name}: $summary")
                                    }
                                }
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Поделиться тренировкой"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GymTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GymSurfaceBorder)
                    ) {
                        // [AI-TASK:TASK-023] Custom Iron Hex Share Record Icon (26dp un-tinted)
                        Icon(painterResource(GymIcons.ShareRecord), contentDescription = null, modifier = Modifier.size(26.dp), tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Поделиться", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDoneClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
                    ) {
                        Text("Готово", color = GymDarkBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
