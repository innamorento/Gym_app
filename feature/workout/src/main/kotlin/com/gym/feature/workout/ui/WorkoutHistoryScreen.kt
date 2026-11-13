package com.gym.feature.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.Workout
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymSurfaceBorder
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import com.gym.core.database.backup.BackupManager
import com.gym.feature.workout.WorkoutViewModel
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Экран истории и журнала всех завершенных тренировок (Workout History Screen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutViewModel,
    onWorkoutClick: (Long) -> Unit,
    onRepeatWorkoutClick: (Workout) -> Unit,
    onStartEmptyWorkoutClick: () -> Unit
) {
    val completedWorkouts by viewModel.completedWorkouts.collectAsState()
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backupManager: BackupManager = koinInject()

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Дневник тренировок",
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                },
                actions = {
                    if (completedWorkouts.isNotEmpty()) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Экспорт тренировок",
                                tint = GymAccentGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymDarkBackground)
            )
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        if (completedWorkouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant)
                            .border(1.dp, GymSurfaceBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.EventNote,
                            contentDescription = null,
                            tint = GymAccentBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "История пока пуста",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Завершите вашу первую тренировку, чтобы видеть здесь динамику объемов и прогресс.",
                        fontSize = 13.sp,
                        color = GymTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStartEmptyWorkoutClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GymDarkBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Быстрый старт", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(completedWorkouts, key = { it.id }) { workout ->
                    val durationMinutes = (workout.durationSeconds ?: 0L) / 60
                    val durationHours = durationMinutes / 60
                    val remMins = durationMinutes % 60
                    val durationStr = if (durationHours > 0) "${durationHours}ч ${remMins}м" else "${remMins}м"
                    val dateStr = dateFormat.format(Date(workout.startTime))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GymSurfaceBorder, RoundedCornerShape(16.dp))
                            .clickable { onWorkoutClick(workout.id) },
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Верхняя строка: Заголовок и кнопка удаления
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = workout.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GymTextPrimary
                                    )
                                    Text(
                                        text = dateStr,
                                        fontSize = 12.sp,
                                        color = GymTextSecondary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { workoutToDelete = workout },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Удалить тренировку",
                                        tint = GymTextSecondary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3 Метрики тренировки
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Время
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GymDarkSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = GymAccentBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = durationStr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GymTextPrimary,
                                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                                    )
                                }

                                // Тоннаж
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GymDarkSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = GymAccentGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${workout.totalVolume.toInt()} кг",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GymTextPrimary,
                                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                                    )
                                }

                                // Подходы
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GymDarkSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GymAccentBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${workout.totalCompletedSets} сетов",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GymTextPrimary,
                                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Превью упражнений
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                workout.exercises.take(3).forEach { ex ->
                                    val comp = ex.sets.filter { it.isCompleted }
                                    val summary = comp.joinToString(", ") { "${it.weight}x${it.reps}" }
                                    Text(
                                        text = "• ${ex.exercise.name}: ${if (summary.isNotBlank()) summary else "${ex.sets.size} сетов"}",
                                        fontSize = 12.sp,
                                        color = GymTextSecondary,
                                        maxLines = 1,
                                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                                    )
                                }
                                if (workout.exercises.size > 3) {
                                    Text(
                                        text = "+ еще ${workout.exercises.size - 3} упражнений...",
                                        fontSize = 11.sp,
                                        color = GymAccentBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Кнопка "Повторить тренировку"
                            Button(
                                onClick = { onRepeatWorkoutClick(workout) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GymDarkSurfaceVariant,
                                    contentColor = GymAccentGreen
                                )
                            ) {
                                Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Повторить тренировку", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог подтверждения удаления тренировки
    workoutToDelete?.let { workout ->
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            containerColor = GymDarkSurface,
            title = {
                Text(
                    text = "Удалить тренировку?",
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
            },
            text = {
                Text(
                    text = "Тренировка «${workout.title}» будет безвозвратно удалена из дневника.",
                    color = GymTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCompletedWorkout(workout.id)
                        workoutToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymAccentRed)
                ) {
                    Text("Удалить", color = GymTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { workoutToDelete = null }) {
                    Text("Отмена", color = GymTextSecondary)
                }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = GymDarkSurface,
            title = {
                Text(
                    text = "Экспорт дневника тренировок",
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Выберите формат для выгрузки истории или отправки в Telegram/почту:",
                        color = GymTextSecondary,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val csv = backupManager.exportWorkoutsCsv()
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "The_Iron_Apex_Workouts.csv")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Экспорт тренировок (CSV)"))
                                showExportDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📊 Таблица CSV (Excel / Google Sheets)", color = GymAccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val json = backupManager.exportBackupJson()
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "The_Iron_Apex_Backup.json")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Полный бэкап (JSON)"))
                                showExportDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📦 Полный резервный бэкап (JSON)", color = GymAccentBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Закрыть", color = GymTextSecondary)
                }
            }
        )
    }
}
