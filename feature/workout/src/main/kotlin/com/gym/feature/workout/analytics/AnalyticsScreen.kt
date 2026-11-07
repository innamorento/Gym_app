package com.gym.feature.workout.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.BalanceStatus
import com.gym.core.model.CompoundPrTrophy
import com.gym.core.model.MuscleCategory
import com.gym.core.model.MuscleGroupBalance
import com.gym.core.model.OneRmTrendPoint
import com.gym.core.model.WeeklyVolumePoint
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
                            text = "Динамика 1ПМ, объем и баланс мышц",
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

            // Интерактивный Canvas график динамики 1RM с точками PR
            item {
                OneRepMaxInteractiveCanvasSection(
                    availableExercises = state.availableExercises,
                    quickChips = state.quickCompoundChips,
                    selectedExerciseId = state.selectedExerciseId,
                    summary = state.selectedExercisePr,
                    onSelectExercise = { viewModel.selectExercise(it) }
                )
            }

            // Интерактивная Canvas столбчатая диаграмма недельного объема
            item {
                WeeklyVolumeCanvasCard(
                    weeklyVolumeHistory = state.weeklyVolumeHistory
                )
            }

            // Анализ баланса мышечных групп (Push / Pull / Legs)
            item {
                state.muscleBalance?.let { balance ->
                    MuscleGroupBalanceCard(balance = balance)
                }
            }

            // Диаграмма распределения нагрузки по отдельным категориям мышц
            item {
                MuscleVolumeBreakdownCard(breakdown = state.muscleBreakdown)
            }

            // Зал славы личных рекордов (PR Trophies)
            item {
                CompoundHallOfFameSection(
                    trophies = state.compoundTrophies
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

        KpiCard(
            modifier = Modifier.weight(1f),
            title = "Тренировок",
            value = "$totalWorkouts",
            subtitle = "завершено",
            icon = Icons.Default.CheckCircle,
            accentColor = GymAccentGreen
        )

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
private fun OneRepMaxInteractiveCanvasSection(
    availableExercises: List<com.gym.core.model.Exercise>,
    quickChips: List<QuickCompoundChip>,
    selectedExerciseId: Long?,
    summary: ExercisePrSummary?,
    onSelectExercise: (Long) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

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
                        text = "Динамика силы (1ПМ)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = "График предельного 1RM по датам и личные рекорды",
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

            // Быстрые чипсы базовых упражнений + кнопка выпадающего списка
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(quickChips) { chip ->
                        val isSelected = chip.exerciseId != null && chip.exerciseId == selectedExerciseId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) GymAccentGreen else GymDarkSurfaceVariant
                                )
                                .clickable(enabled = chip.exerciseId != null) {
                                    chip.exerciseId?.let { onSelectExercise(it) }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chip.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else GymTextPrimary
                            )
                        }
                    }
                }

                // Выпадающий список всех упражнений
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(GymDarkSurfaceVariant)
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Все", fontSize = 11.sp, color = GymAccentBlue, fontWeight = FontWeight.Bold)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Выбрать упражнение",
                                tint = GymAccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(GymDarkSurface)
                    ) {
                        availableExercises.forEach { ex ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = ex.name,
                                        color = if (ex.id == selectedExerciseId) GymAccentGreen else GymTextPrimary,
                                        fontWeight = if (ex.id == selectedExerciseId) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSelectExercise(ex.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (summary != null && summary.best1Rm > 0.0) {
                // Карточка текущего рекорда PR
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GymTextPrimary
                            )
                            Text(
                                text = "из сета ${summary.bestWeight} кг × ${summary.bestReps} повт.",
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
                                text = "${summary.exerciseName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymAccentGreen
                            )
                        }
                    }
                }

                // Интерактивный Jetpack Compose Canvas график динамики 1RM
                Text(
                    text = "График 1RM во времени",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GymTextPrimary
                )

                OneRepMaxCanvasLineChart(trendPoints = summary.trendPoints)

                // Хронология подходов
                val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("ru"))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.records.takeLast(4).reversed().forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymDarkSurfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (record.isPr) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Рекорд",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = dateFormat.format(Date(record.workoutDate)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GymTextPrimary
                                    )
                                    Text(
                                        text = "${record.weight} кг × ${record.reps} повт.",
                                        fontSize = 11.sp,
                                        color = GymTextSecondary
                                    )
                                }
                            }

                            Text(
                                text = "1RM: ${record.estimated1Rm} кг",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (record.isPr) Color(0xFFFFD700) else GymAccentBlue
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "По этому упражнению еще нет завершенных рабочих сетов.",
                        color = GymTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OneRepMaxCanvasLineChart(trendPoints: List<OneRmTrendPoint>) {
    if (trendPoints.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GymDarkBackground.copy(alpha = 0.7f))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val min1Rm = trendPoints.minOf { it.estimated1Rm } * 0.9
            val max1Rm = trendPoints.maxOf { it.estimated1Rm } * 1.1
            val range = if (max1Rm > min1Rm) (max1Rm - min1Rm).toFloat() else 1f

            val stepX = if (trendPoints.size > 1) width / (trendPoints.size - 1) else width / 2

            // Отрисовка горизонтальных пунктирных линий сетки
            val gridLinesCount = 3
            for (i in 0..gridLinesCount) {
                val y = height * (i.toFloat() / gridLinesCount)
                drawLine(
                    color = GymDarkSurfaceVariant,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // Построение линии графика
            val linePath = Path()
            val fillPath = Path()

            trendPoints.forEachIndexed { index, point ->
                val x = if (trendPoints.size == 1) width / 2 else index * stepX
                val normalizedY = ((point.estimated1Rm - min1Rm) / range).toFloat()
                val y = height - (normalizedY * height).coerceIn(0f, height)

                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (index == trendPoints.lastIndex) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Градиентная заливка под графиком
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GymAccentGreen.copy(alpha = 0.35f),
                        GymAccentBlue.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )

            // Основная линия графика
            drawPath(
                path = linePath,
                color = GymAccentGreen,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Точки на графике
            trendPoints.forEachIndexed { index, point ->
                val x = if (trendPoints.size == 1) width / 2 else index * stepX
                val normalizedY = ((point.estimated1Rm - min1Rm) / range).toFloat()
                val y = height - (normalizedY * height).coerceIn(0f, height)

                if (point.isPersonalRecord) {
                    // Золотая точка рекорда с ореолом
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.3f),
                        radius = 8.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 4.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                } else {
                    // Обычная точка тренировки
                    drawCircle(
                        color = GymAccentBlue,
                        radius = 3.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyVolumeCanvasCard(weeklyVolumeHistory: List<WeeklyVolumePoint>) {
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
                        text = "Динамика тоннажа по неделям",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = "Объем поднятого веса за последние 4 недели",
                        fontSize = 11.sp,
                        color = GymTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = GymAccentBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (weeklyVolumeHistory.isEmpty() || weeklyVolumeHistory.all { it.totalVolumeKg == 0.0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Завершите тренировки за текущую неделю для построения гистограммы объема.",
                        color = GymTextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GymDarkBackground.copy(alpha = 0.7f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxVolume = (weeklyVolumeHistory.maxOf { it.totalVolumeKg } * 1.25).coerceAtLeast(100.0)
                        val barWidth = 28.dp.toPx()
                        val spacing = (size.width - (barWidth * weeklyVolumeHistory.size)) / (weeklyVolumeHistory.size + 1)

                        weeklyVolumeHistory.forEachIndexed { index, weekPoint ->
                            val left = spacing + index * (barWidth + spacing)
                            val barHeight = ((weekPoint.totalVolumeKg / maxVolume) * (size.height - 24.dp.toPx())).toFloat()
                            val top = size.height - barHeight - 16.dp.toPx()

                            // Отрисовка столбца с градиентом
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(GymAccentBlue, GymAccentGreen)
                                ),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }

                    // Подписи под столбцами
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        weeklyVolumeHistory.forEach { point ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${point.totalVolumeKg.toInt()} кг",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymAccentGreen
                                )
                                Text(
                                    text = point.weekLabel,
                                    fontSize = 9.sp,
                                    color = GymTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MuscleGroupBalanceCard(balance: MuscleGroupBalance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Баланс нагрузок (Push / Pull / Legs)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )

                // Бейдж статуса баланса
                val (badgeBg, badgeText) = when (balance.balanceStatus) {
                    BalanceStatus.OPTIMAL -> Pair(GymAccentGreen.copy(alpha = 0.2f), GymAccentGreen)
                    BalanceStatus.PUSH_DOMINANT -> Pair(Color(0xFFFF9100).copy(alpha = 0.2f), Color(0xFFFF9100))
                    BalanceStatus.PULL_DEFICIT, BalanceStatus.LEGS_DEFICIT -> Pair(Color(0xFFFF5252).copy(alpha = 0.2f), Color(0xFFFF5252))
                    BalanceStatus.NEEDS_MORE_DATA -> Pair(GymDarkSurfaceVariant, GymTextSecondary)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = balance.balanceStatus.titleRu,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }
            }

            // Многосегментная полоса распределения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(GymDarkSurfaceVariant)
            ) {
                if (balance.pushPercentage > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(balance.pushPercentage)
                            .background(GymAccentBlue)
                    )
                }
                if (balance.pullPercentage > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(balance.pullPercentage)
                            .background(Color(0xFFB388FF))
                    )
                }
                if (balance.legsPercentage > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(balance.legsPercentage)
                            .background(Color(0xFFFF5252))
                    )
                }
            }

            // Легенда с процентами
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(GymAccentBlue, CircleShape))
                    Text(text = "Push: ${balance.pushPercentage}%", fontSize = 11.sp, color = GymTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFB388FF), CircleShape))
                    Text(text = "Pull: ${balance.pullPercentage}%", fontSize = 11.sp, color = GymTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5252), CircleShape))
                    Text(text = "Legs: ${balance.legsPercentage}%", fontSize = 11.sp, color = GymTextSecondary)
                }
            }

            // Тренерская рекомендация
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GymDarkSurfaceVariant)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GymAccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = balance.recommendation,
                        fontSize = 11.sp,
                        color = GymTextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MuscleVolumeBreakdownCard(breakdown: List<MuscleCategoryVolume>) {
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

@Composable
private fun CompoundHallOfFameSection(trophies: List<CompoundPrTrophy>) {
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
                        text = "🏆 Зал Славы (PR Trophies)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "Максимальные достижения в базовых движениях",
                        fontSize = 11.sp,
                        color = GymTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
            }

            if (trophies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Выполните жим, приседания или тягу, чтобы открыть трофеи зала славы.",
                        color = GymTextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                trophies.forEach { trophy ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trophy.exerciseName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymTextPrimary
                                )
                                Text(
                                    text = "Пиковый 1RM: ${trophy.best1Rm} кг",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TrophyStatChip(label = "Макс. вес", value = "${trophy.maxWeight} кг × ${trophy.maxWeightReps}")
                                trophy.max3RepsWeight?.let {
                                    TrophyStatChip(label = "3RM", value = "$it кг")
                                }
                                trophy.max5RepsWeight?.let {
                                    TrophyStatChip(label = "5RM", value = "$it кг")
                                }
                                trophy.max10RepsWeight?.let {
                                    TrophyStatChip(label = "10RM", value = "$it кг")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophyStatChip(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GymDarkBackground)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 9.sp, color = GymTextSecondary)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GymTextPrimary)
        }
    }
}
