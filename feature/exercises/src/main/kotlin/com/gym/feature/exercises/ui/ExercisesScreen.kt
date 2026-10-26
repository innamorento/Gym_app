package com.gym.feature.exercises.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import com.gym.core.ui.theme.GymAccentGreen
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GymTextPrimary,
                    unfocusedTextColor = GymTextPrimary,
                    focusedBorderColor = GymAccentGreen,
                    unfocusedBorderColor = GymDarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Фильтр по группам мышц (горизонтальный скролл)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedMuscleGroup == null,
                        onClick = { viewModel.onMuscleGroupSelected(null) },
                        label = { Text("Все") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymAccentGreen,
                            selectedLabelColor = GymDarkBackground,
                            containerColor = GymDarkSurface,
                            labelColor = GymTextSecondary
                        )
                    )
                }

                items(MuscleGroup.values()) { group ->
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

            Spacer(modifier = Modifier.height(8.dp))

            // Список упражнений
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GymAccentGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.exercises, key = { it.id }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onClick = { onExerciseSelect(exercise) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = exercise.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = exercise.primaryMuscleGroup.displayNameRu,
                    fontSize = 12.sp,
                    color = GymAccentGreen
                )
                Text(
                    text = " • ${exercise.equipment.displayNameRu}",
                    fontSize = 12.sp,
                    color = GymTextSecondary
                )
            }
        }
    }
}
