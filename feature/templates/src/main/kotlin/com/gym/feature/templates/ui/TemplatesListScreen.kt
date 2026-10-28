package com.gym.feature.templates.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoCameraFront
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
import com.gym.feature.templates.TemplatesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesListScreen(
    viewModel: TemplatesViewModel,
    onCreateTemplateClick: () -> Unit,
    onTemplateClick: (Long) -> Unit,
    onStartWorkoutClick: (Long) -> Unit,
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onCoachReviewsClick: () -> Unit = {}
) {
    val state by viewModel.listUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Мои тренировки",
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                },
                actions = {
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
                onCreateClick = onCreateTemplateClick
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.templates, key = { it.id }) { template ->
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    if (!template.description.isNullOrBlank()) {
                        Text(
                            text = template.description.orEmpty(),
                            fontSize = 13.sp,
                            color = GymTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onStartClick,
                    modifier = Modifier
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

            Spacer(modifier = Modifier.height(12.dp))

            if (hasSupersets) {
                SupersetBadge(groupLabel = "Содержит суперсеты")
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = template.exercises.joinToString(" • ") { it.exercise.name },
                fontSize = 13.sp,
                color = GymTextSecondary,
                maxLines = 2
            )
        }
    }
}

@Composable
fun EmptyTemplatesPlaceholder(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
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
            text = "Создайте программу тренировок (День А, День B) и объединяйте упражнения в суперсеты",
            fontSize = 14.sp,
            color = GymTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
