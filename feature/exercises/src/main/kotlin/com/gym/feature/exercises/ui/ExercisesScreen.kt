package com.gym.feature.exercises.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleGroup
import com.gym.core.model.MuscleRegion
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import com.gym.feature.exercises.ExercisesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    viewModel: ExercisesViewModel,
    onBackClick: () -> Unit,
    onExerciseSelect: (Exercise) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

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

            // 1-й Уровень: 4 макро-хаба (сетка 2x2 без бесконечного скролла)
            Text(
                text = "Группы мышц (выберите категорию):",
                fontSize = 12.sp,
                color = GymTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MuscleRegionCard(
                        region = MuscleRegion.PUSH,
                        count = state.regionCounts[MuscleRegion.PUSH] ?: 0,
                        isSelected = state.selectedRegion == MuscleRegion.PUSH,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.onRegionSelected(if (state.selectedRegion == MuscleRegion.PUSH) null else MuscleRegion.PUSH)
                        }
                    )
                    MuscleRegionCard(
                        region = MuscleRegion.PULL,
                        count = state.regionCounts[MuscleRegion.PULL] ?: 0,
                        isSelected = state.selectedRegion == MuscleRegion.PULL,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.onRegionSelected(if (state.selectedRegion == MuscleRegion.PULL) null else MuscleRegion.PULL)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MuscleRegionCard(
                        region = MuscleRegion.LEGS,
                        count = state.regionCounts[MuscleRegion.LEGS] ?: 0,
                        isSelected = state.selectedRegion == MuscleRegion.LEGS,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.onRegionSelected(if (state.selectedRegion == MuscleRegion.LEGS) null else MuscleRegion.LEGS)
                        }
                    )
                    MuscleRegionCard(
                        region = MuscleRegion.CORE,
                        count = state.regionCounts[MuscleRegion.CORE] ?: 0,
                        isSelected = state.selectedRegion == MuscleRegion.CORE,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.onRegionSelected(if (state.selectedRegion == MuscleRegion.CORE) null else MuscleRegion.CORE)
                        }
                    )
                }
            }

            // 2-й Уровень: Компактные подгруппы выбранного хаба
            if (state.selectedRegion != null) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedMuscleGroup == null,
                            onClick = { viewModel.onMuscleGroupSelected(null) },
                            label = { Text("Все в хабе") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentGreen,
                                selectedLabelColor = GymDarkBackground,
                                containerColor = GymDarkSurface,
                                labelColor = GymTextSecondary
                            )
                        )
                    }

                    items(state.selectedRegion!!.muscleGroups) { group ->
                        FilterChip(
                            selected = state.selectedMuscleGroup == group,
                            onClick = { viewModel.onMuscleGroupSelected(group) },
                            label = { Text(group.displayNameRu) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentGreen,
                                selectedLabelColor = GymDarkBackground,
                                containerColor = GymDarkSurface,
                                labelColor = GymTextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Список упражнений
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GymAccentGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.exercises, key = { it.id }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onClick = { onExerciseSelect(exercise) },
                            onExclude = { viewModel.excludeExercise(exercise.id) },
                            onRestore = { viewModel.restoreExercise(exercise.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleRegionCard(
    region: MuscleRegion,
    count: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, GymAccentGreen, RoundedCornerShape(12.dp)) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GymDarkSurfaceVariant else GymDarkSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = region.emoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = region.displayNameRu.substringBefore(" ("),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GymAccentGreen else GymTextPrimary
                )
            }
            Text(
                text = "$count",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GymTextSecondary
            )
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit,
    onExclude: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { if (!exercise.isExcluded) onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.isExcluded) GymDarkSurface.copy(alpha = 0.5f) else GymDarkSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (exercise.isExcluded) GymTextSecondary else GymTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = exercise.primaryMuscleGroup.displayNameRu,
                        fontSize = 12.sp,
                        color = if (exercise.isExcluded) GymTextSecondary else GymAccentGreen
                    )
                    Text(
                        text = " • ${exercise.equipment.displayNameRu}",
                        fontSize = 12.sp,
                        color = GymTextSecondary
                    )
                }

                if (exercise.isExcluded) {
                    Spacer(modifier = Modifier.height(4.dp))
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
                IconButton(onClick = onExclude) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Скрыть / Исключить (нет в зале)",
                        tint = GymTextSecondary
                    )
                }
            }
        }
    }
}
