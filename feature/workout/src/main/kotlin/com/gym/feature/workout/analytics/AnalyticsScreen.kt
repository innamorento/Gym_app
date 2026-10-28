package com.gym.feature.workout.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.MuscleCategory
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Аналитика и прогресс",
                            fontWeight = FontWeight.Bold,
                            color = GymTextPrimary,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Тоннаж, 1ПМ и баланс мышц",
                            color = GymTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = GymTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GymDarkBackground
                )
            )
        },
        containerColor = GymDarkBackground,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Переключатель временного периода
            item {
                TimeRangeSelector(
                    selectedRange = state.selectedTimeRange,
                    onSelectRange = { viewModel.selectTimeRange(it) }
                )
            }

            // Главные KPI показатели (Тоннаж, Тренировки, Подходы)
            item {
                KpiSummaryCards(
                    totalVolumeKg = state.totalVolumeKg,
                    totalWorkouts = state.totalWorkoutsCount,
                    totalSets = state.totalCompletedSets
                )
            }

            // Диаграмма распределения нагрузки по группам мышц
            item {
                MuscleVolumeBreakdownCard(breakdown = state.muscleBreakdown)
            }

            // Раздел 1ПМ и рекордов
            item {
                OneRepMaxSection(
                    availableExercises = state.availableExercises,
                    selectedExerciseId = state.selectedExerciseId,
                    summary = state.selectedExercisePr,
                    onSelectExercise = { viewModel.selectExercise(it) }
                )
            }
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: AnalyticsTimeRange,
    onSelectRange: (AnalyticsTimeRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnalyticsTimeRange.values().forEach { range ->
            val isSelected = range == selectedRange
            FilterChip(
                selected = isSelected,
                onClick = { onSelectRange(range) },
                label = {
                    Text(
                        text = range.titleRu,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = GymDarkSurface,
                    labelColor = GymTextSecondary,
                    selectedContainerColor = GymAccentBlue,
                    selectedLabelColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                border = null
            )
        }
    }
}

@Composable
private fun KpiSummaryCards(
    totalVolumeKg: Double,
    totalWorkouts: Int,
    totalSets: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Карточка тоннажа
        KpiCard(
            modifier = Modifier.weight(1.3f),
            title = "Общий тоннаж",
            value = if (totalVolumeKg >= 1000.0) {
                String.format(Locale.US, "%.1f т", totalVolumeKg / 1000.0)
            } else {
                "${totalVolumeKg.toInt()} кг"
            },
            subtitle = "${totalVolumeKg.toInt()} кг суммарно",
            icon = Icons.Default.FitnessCenter,
            accentColor = GymAccentBlue
        )

        // Карточка тренировок
        KpiCard(
            modifier = Modifier.weight(1f),
            title = "Тренировок",
            value = "$totalWorkouts",
            subtitle = "завершено",
            icon = Icons.Default.CheckCircle,
            accentColor = GymAccentGreen
        )

        // Карточка подходов
        KpiCard(
            modifier = Modifier.weight(1f),
            title = "Подходов",
            value = "$totalSets",
            subtitle = "рабочих сетов",
            icon = Icons.Default.Repeat,
            accentColor = Color(0xFFFFB300)
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = GymTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = GymTextSecondary
            )
        }
    }
}

@Composable
private fun MuscleVolumeBreakdownCard(
    breakdown: List<MuscleCategoryVolume>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Распределение по мышцам",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = GymAccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (breakdown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Завершите тренировку, чтобы увидеть баланс нагрузок по группам мышц.",
                        color = GymTextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                breakdown.forEach { item ->
                    MuscleProgressRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun MuscleProgressRow(item: MuscleCategoryVolume) {
    val barColor = when (item.category) {
        MuscleCategory.CHEST -> GymAccentBlue
        MuscleCategory.BACK -> Color(0xFFB388FF)
        MuscleCategory.LEGS -> Color(0xFFFF5252)
        MuscleCategory.SHOULDERS -> Color(0xFFFFD700)
        MuscleCategory.BICEPS, MuscleCategory.TRICEPS, MuscleCategory.ARMS -> GymAccentGreen
        MuscleCategory.CORE -> Color(0xFFFF9100)
        MuscleCategory.OTHER -> Color(0xFF90A4AE)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = item.category.emoji, fontSize = 14.sp)
                Text(
                    text = item.category.displayNameRu,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GymTextPrimary
                )
                Text(
                    text = "· ${item.percentage}%",
                    fontSize = 12.sp,
                    color = GymTextSecondary
                )
            }
            Text(
                text = "${item.volumeKg.toInt()} кг (${item.setsCount} п.)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GymTextSecondary
            )
        }

        LinearProgressIndicator(
            progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = GymDarkSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OneRepMaxSection(
    availableExercises: List<com.gym.core.model.Exercise>,
    selectedExerciseId: Long?,
    summary: ExercisePrSummary?,
    onSelectExercise: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Силовой максимум (1ПМ)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = "Расчет предельного 1RM по формуле Эпли: W × (1 + R/30)",
                        fontSize = 11.sp,
                        color = GymTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = GymAccentGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Горизонтальный список выбора упражнения
            if (availableExercises.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableExercises) { exercise ->
                        val isSelected = exercise.id == selectedExerciseId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) GymAccentGreen else GymDarkSurfaceVariant)
                                .clickable { onSelectExercise(exercise.id) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = exercise.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else GymTextPrimary
                            )
                        }
                    }
                }
            }

            // Карточка рекорда 1ПМ
            if (summary != null && summary.best1Rm > 0.0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ЛИЧНЫЙ РЕКОРД (PR)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700),
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "${summary.best1Rm} кг",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GymTextPrimary
                            )
                            Text(
                                text = "из подхода ${summary.bestWeight} кг × ${summary.bestReps} повт.",
                                fontSize = 11.sp,
                                color = GymTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymAccentGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "1RM Peak",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymAccentGreen
                            )
                        }
                    }
                }

                // История тренировок по этому упражнению
                Text(
                    text = "Динамика по тренировкам",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GymTextPrimary
                )

                val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("ru"))
                summary.records.takeLast(5).reversed().forEach { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GymDarkSurfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dateFormat.format(Date(record.workoutDate)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = GymTextPrimary
                            )
                            Text(
                                text = "Сет: ${record.weight} кг × ${record.reps} повт.",
                                fontSize = 11.sp,
                                color = GymTextSecondary
                            )
                        }

                        Text(
                            text = "1RM: ${record.estimated1Rm} кг",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymAccentBlue
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "По этому упражнению еще нет завершенных рабочих подходов.",
                        color = GymTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
