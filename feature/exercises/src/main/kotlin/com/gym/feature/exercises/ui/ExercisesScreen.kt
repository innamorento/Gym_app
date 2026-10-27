package com.gym.feature.exercises.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.Equipment
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleCategory
import com.gym.core.ui.component.ExerciseDetailSheet
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentPurple
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import com.gym.feature.exercises.ExercisesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExercisesScreen(
    viewModel: ExercisesViewModel,
    onBackClick: () -> Unit,
    onExerciseSelect: (Exercise) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedExerciseForDetail by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Каталог упражнений",
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
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
                actions = {
                    IconButton(onClick = viewModel::toggleShowExcluded) {
                        Icon(
                            imageVector = if (state.showExcluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Показать скрытые/исключенные",
                            tint = if (state.showExcluded) GymAccentGreen else GymTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymDarkBackground)
            )
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Поисковая строка
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Поиск упражнения...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = GymTextSecondary)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить", tint = GymTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GymTextPrimary,
                    unfocusedTextColor = GymTextPrimary,
                    focusedBorderColor = GymAccentGreen,
                    unfocusedBorderColor = GymDarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Понятные анатомические категории мышц с FlowRow автопереносом
            Text(
                text = "Группы мышц:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GymTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Все упражнения
                FilterChip(
                    selected = state.selectedRegion == null,
                    onClick = { viewModel.onRegionSelected(null) },
                    label = { Text("Все (${state.exercises.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GymAccentGreen,
                        selectedLabelColor = GymDarkBackground,
                        containerColor = GymDarkSurface,
                        labelColor = GymTextSecondary
                    )
                )

                // Понятные привычные категории
                MuscleCategory.values().forEach { category ->
                    val count = state.regionCounts[category] ?: 0
                    FilterChip(
                        selected = state.selectedRegion == category,
                        onClick = {
                            viewModel.onRegionSelected(if (state.selectedRegion == category) null else category)
                        },
                        label = {
                            Text("${category.emoji} ${category.displayNameRu} ($count)", fontSize = 12.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymAccentGreen,
                            selectedLabelColor = GymDarkBackground,
                            containerColor = GymDarkSurface,
                            labelColor = GymTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Список упражнений
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GymAccentGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.exercises, key = { it.id }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onClick = {
                                selectedExerciseForDetail = exercise
                            },
                            onQuickAdd = {
                                onExerciseSelect(exercise)
                            },
                            onExclude = { viewModel.excludeExercise(exercise.id) },
                            onRestore = { viewModel.restoreExercise(exercise.id) }
                        )
                    }
                }
            }
        }
    }

    // Модальная карточка упражнения с описанием, иллюстрацией техники и заготовкой под 3D анимацию
    if (selectedExerciseForDetail != null) {
        ExerciseDetailSheet(
            exercise = selectedExerciseForDetail,
            onDismiss = { selectedExerciseForDetail = null },
            onSelectExercise = { exercise ->
                onExerciseSelect(exercise)
                selectedExerciseForDetail = null
            }
        )
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit,
    onExclude: () -> Unit,
    onRestore: () -> Unit
) {
    val iconBgGradient = when (exercise.equipment) {
        Equipment.BARBELL -> Brush.linearGradient(listOf(GymAccentGreen.copy(alpha = 0.25f), GymAccentBlue.copy(alpha = 0.15f)))
        Equipment.DUMBBELL -> Brush.linearGradient(listOf(GymAccentBlue.copy(alpha = 0.25f), GymAccentPurple.copy(alpha = 0.15f)))
        Equipment.CABLE -> Brush.linearGradient(listOf(GymAccentPurple.copy(alpha = 0.25f), GymDarkSurfaceVariant))
        Equipment.MACHINE -> Brush.linearGradient(listOf(Color(0xFFEAB308).copy(alpha = 0.25f), GymDarkSurfaceVariant))
        Equipment.BODYWEIGHT -> Brush.linearGradient(listOf(Color(0xFF06B6D4).copy(alpha = 0.25f), GymDarkSurfaceVariant))
        else -> Brush.linearGradient(listOf(GymDarkSurfaceVariant, GymDarkBackground))
    }

    val equipmentBadgeEmoji = when (exercise.equipment) {
        Equipment.BARBELL -> "🏋️"
        Equipment.DUMBBELL -> "💪"
        Equipment.CABLE -> "⚙️"
        Equipment.MACHINE -> "🏗️"
        Equipment.BODYWEIGHT -> "🤸"
        else -> "🎯"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { if (!exercise.isExcluded) onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.isExcluded) GymDarkSurface.copy(alpha = 0.5f) else GymDarkSurface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgGradient)
                    .border(1.dp, GymDarkSurfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = equipmentBadgeEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (exercise.isExcluded) GymTextSecondary else GymTextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exercise.primaryMuscleGroup.displayNameRu,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (exercise.isExcluded) GymTextSecondary else GymAccentGreen
                    )
                    Text(
                        text = " • ${exercise.equipment.displayNameRu}",
                        fontSize = 11.sp,
                        color = GymTextSecondary
                    )
                }

                if (exercise.isExcluded) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Исключено (${exercise.exclusionReason ?: "Не используется"})",
                        fontSize = 11.sp,
                        color = GymAccentRed
                    )
                }
            }

            if (exercise.isExcluded) {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Вернуть в базу",
                        tint = GymAccentGreen
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Подробнее об упражнении",
                            tint = GymTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onQuickAdd,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GymAccentGreen.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить в тренировку",
                            tint = GymAccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onExclude) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Скрыть",
                            tint = GymTextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
