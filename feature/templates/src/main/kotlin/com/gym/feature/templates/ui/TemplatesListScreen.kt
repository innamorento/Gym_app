package com.gym.feature.templates.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.gym.core.model.WorkoutTemplate
import com.gym.core.ui.component.SupersetBadge
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gym.core.model.TrainingExperience
import com.gym.core.ui.component.ExerciseThumbnail
import com.gym.core.ui.component.GymBrandLogo
import com.gym.core.ui.theme.GymAccentPurple
import com.gym.feature.templates.TemplatesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesListScreen(
    viewModel: TemplatesViewModel,
    onCreateTemplateClick: () -> Unit,
    onTemplateClick: (Long) -> Unit,
    onStartWorkoutClick: (Long) -> Unit,
    onStartEmptyWorkoutClick: () -> Unit = {},
    activeWorkoutTitle: String? = null,
    onResumeWorkoutClick: (() -> Unit)? = null,
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onCoachReviewsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val state by viewModel.listUiState.collectAsState()
    var selectedLevelFilter by remember { mutableStateOf<TrainingExperience?>(null) }
    val displayedTemplates = remember(state.templates, selectedLevelFilter) {
        if (selectedLevelFilter == null) {
            state.templates
        } else {
            state.templates.filter { it.experienceLevel == selectedLevelFilter }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GymBrandLogo(
                        size = 36.dp,
                        showWordmark = true,
                        subtitle = "Тренировочные программы"
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Профиль атлета",
                            tint = GymTextPrimary
                        )
                    }
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Аналитика и прогресс",
                            tint = GymAccentGreen
                        )
                    }
                    IconButton(onClick = onCoachReviewsClick) {
                        Icon(
                            imageVector = Icons.Default.VideoCameraFront,
                            contentDescription = "Кабинет тренера: видеоотчеты",
                            tint = GymAccentBlue
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Настройки оборудования",
                            tint = GymTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GymDarkBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTemplateClick,
                containerColor = GymAccentGreen,
                contentColor = GymDarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать шаблон")
            }
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GymAccentGreen)
            }
        } else if (state.templates.isEmpty()) {
            EmptyTemplatesPlaceholder(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onCreateClick = onCreateTemplateClick,
                onStartEmptyClick = onStartEmptyWorkoutClick
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeWorkoutTitle != null && onResumeWorkoutClick != null) {
                    item(key = "active_workout_banner") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onResumeWorkoutClick() },
                            colors = CardDefaults.cardColors(containerColor = GymAccentGreen.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, GymAccentGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⚡ ИДЕТ ТРЕНИРОВКА",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GymAccentGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = activeWorkoutTitle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GymTextPrimary
                                    )
                                }
                                Button(
                                    onClick = onResumeWorkoutClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Вернуться", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item(key = "quick_start_button") {
                    Button(
                        onClick = onStartEmptyWorkoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GymDarkSurfaceVariant,
                            contentColor = GymAccentGreen
                        ),
                        border = BorderStroke(1.dp, GymAccentGreen.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Быстрый старт (Свободная тренировка)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Фильтры по уровню подготовки (Все, Новичок, Средний, Опытный)
                item(key = "level_filter_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedLevelFilter == null,
                            onClick = { selectedLevelFilter = null },
                            label = { Text("Все (${state.templates.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentGreen,
                                selectedLabelColor = GymDarkBackground,
                                containerColor = GymDarkSurfaceVariant,
                                labelColor = GymTextPrimary
                            )
                        )
                        FilterChip(
                            selected = selectedLevelFilter == TrainingExperience.BEGINNER,
                            onClick = { selectedLevelFilter = TrainingExperience.BEGINNER },
                            label = { Text("🟢 Новичок", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentGreen,
                                selectedLabelColor = GymDarkBackground,
                                containerColor = GymDarkSurfaceVariant,
                                labelColor = GymTextPrimary
                            )
                        )
                        FilterChip(
                            selected = selectedLevelFilter == TrainingExperience.INTERMEDIATE,
                            onClick = { selectedLevelFilter = TrainingExperience.INTERMEDIATE },
                            label = { Text("🔵 Средний", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentBlue,
                                selectedLabelColor = GymDarkBackground,
                                containerColor = GymDarkSurfaceVariant,
                                labelColor = GymTextPrimary
                            )
                        )
                        FilterChip(
                            selected = selectedLevelFilter == TrainingExperience.ADVANCED,
                            onClick = { selectedLevelFilter = TrainingExperience.ADVANCED },
                            label = { Text("🟣 Профи", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentPurple,
                                selectedLabelColor = GymDarkBackground,
                                containerColor = GymDarkSurfaceVariant,
                                labelColor = GymTextPrimary
                            )
                        )
                    }
                }

                items(displayedTemplates, key = { it.id }) { template ->
                    TemplateCardItem(
                        template = template,
                        onClick = { onTemplateClick(template.id) },
                        onStartClick = { onStartWorkoutClick(template.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateCardItem(
    template: WorkoutTemplate,
    onClick: () -> Unit,
    onStartClick: () -> Unit
) {
    val hasSupersets = template.exercises.any { it.isPartOfSuperset }

    val levelBadgeColor = when (template.experienceLevel) {
        TrainingExperience.BEGINNER -> GymAccentGreen
        TrainingExperience.INTERMEDIATE -> GymAccentBlue
        TrainingExperience.ADVANCED -> GymAccentPurple
    }
    val levelBadgeText = when (template.experienceLevel) {
        TrainingExperience.BEGINNER -> "🟢 Новичок"
        TrainingExperience.INTERMEDIATE -> "🔵 Средний"
        TrainingExperience.ADVANCED -> "🟣 Профи"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GymDarkSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Уровень подготовки + бейджи
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(levelBadgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = levelBadgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelBadgeColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GymDarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${template.exercises.size} упр.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GymTextSecondary
                        )
                    }

                    if (hasSupersets) {
                        SupersetBadge(groupLabel = "Суперсеты")
                    }
                }

                IconButton(
                    onClick = onStartClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GymAccentGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Начать тренировку",
                        tint = GymDarkBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = template.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary
            )

            if (!template.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = template.description.orEmpty(),
                    fontSize = 13.sp,
                    color = GymTextSecondary,
                    lineHeight = 18.sp
                )
            }

            // Горизонтальный ряд миниатюр упражнений программы
            if (template.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    template.exercises.forEach { item ->
                        ExerciseThumbnail(
                            thumbnailUrl = item.exercise.thumbnailUrl,
                            gifUrl = item.exercise.gifUrl,
                            primaryMuscleGroup = item.exercise.primaryMuscleGroup,
                            equipment = item.exercise.equipment,
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentDescription = item.exercise.name
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.exercises.joinToString(" • ") { it.exercise.name },
                fontSize = 12.sp,
                color = GymTextSecondary.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun EmptyTemplatesPlaceholder(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit,
    onStartEmptyClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = GymDarkSurfaceVariant,
            modifier = Modifier.height(72.dp).width(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "У вас пока нет шаблонов",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = GymTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Создайте программу тренировок или начните свободную тренировку прямо сейчас",
            fontSize = 14.sp,
            color = GymTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onStartEmptyClick,
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GymDarkBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Свободная", color = GymDarkBackground, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant, contentColor = GymTextPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Создать шаблон")
            }
        }
    }
}
