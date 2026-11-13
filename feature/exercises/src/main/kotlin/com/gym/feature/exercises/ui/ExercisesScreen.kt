package com.gym.feature.exercises.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.Equipment
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleCategory
import com.gym.core.ui.component.ExerciseDetailSheet
import com.gym.core.ui.component.ExerciseThumbnail
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
    var showCreateDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Создать упражнение",
                            tint = GymAccentGreen
                        )
                    }
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = GymAccentGreen,
                contentColor = GymDarkBackground,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Создать упражнение"
                )
            }
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

            // Если группа мышц не выбрана — показываем все категории
            if (state.selectedRegion == null) {
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
                        selected = true,
                        onClick = { viewModel.onRegionSelected(null) },
                        label = { Text("Все (${state.exercises.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymAccentGreen,
                            selectedLabelColor = GymDarkBackground,
                            containerColor = GymDarkSurface,
                            labelColor = GymTextSecondary
                        )
                    )

                    // Привычные анатомические категории
                    MuscleCategory.values().forEach { category ->
                        val count = state.regionCounts[category] ?: 0
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.onRegionSelected(category) },
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
            } else {
                // Когда группа выбрана: прячем остальные группы, даем кнопку «Назад» и компактный бейдж
                val currentCategory = state.selectedRegion
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GymDarkSurfaceVariant)
                            .clickable { viewModel.onRegionSelected(null) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Все группы мышц",
                            tint = GymAccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Все группы",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GymTextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GymAccentGreen.copy(alpha = 0.15f))
                            .border(1.dp, GymAccentGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentCategory?.emoji} ${currentCategory?.displayNameRu}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${state.exercises.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GymTextSecondary
                        )
                    }
                }

                // Горизонтальный фильтр по типу снаряда ("свободный вес", гантели, штанга и т.д.)
                val equipmentList = listOf(
                    null to "Все снаряды",
                    Equipment.BARBELL to "🏋️ Штанга",
                    Equipment.DUMBBELL to "💪 Гантели",
                    Equipment.BODYWEIGHT to "🤸 Свободный вес",
                    Equipment.CABLE to "⚡ Блок / Кроссовер",
                    Equipment.MACHINE to "⚙️ Тренажер",
                    Equipment.WEIGHTED_BODYWEIGHT to "🎒 С весом",
                    Equipment.KETTLEBELL to "🔔 Гиря",
                    Equipment.RESISTANCE_BAND to "🎗️ Эспандер",
                    Equipment.OTHER to "📦 Другое"
                ).filter { (eq, _) ->
                    eq == null || (state.equipmentCounts[eq] ?: 0) > 0
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    equipmentList.forEach { (eq, title) ->
                        val isSelected = state.selectedEquipment == eq
                        val count = if (eq == null) {
                            state.equipmentCounts.values.sum()
                        } else {
                            state.equipmentCounts[eq] ?: 0
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onEquipmentSelected(if (isSelected) null else eq) },
                            label = {
                                Text(
                                    text = if (count > 0 && eq != null) "$title ($count)" else title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
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
            }

            Spacer(modifier = Modifier.height(6.dp))

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

    // Модальная карточка упражнения с описанием, иллюстрацией техники и удалением
    if (selectedExerciseForDetail != null) {
        ExerciseDetailSheet(
            exercise = selectedExerciseForDetail,
            onDismiss = { selectedExerciseForDetail = null },
            onSelectExercise = { exercise ->
                onExerciseSelect(exercise)
                selectedExerciseForDetail = null
            },
            onDeleteExercise = { exercise ->
                viewModel.deleteCustomExercise(exercise.id)
                selectedExerciseForDetail = null
            }
        )
    }

    // Модальный конструктор нового пользовательского упражнения
    if (showCreateDialog) {
        CreateExerciseDialog(
            onDismiss = { showCreateDialog = false },
            onCreateExercise = { name, muscleGroup, equipment, defaultRestSeconds, initialPosition, execution, commonMistakes ->
                viewModel.createCustomExercise(
                    name = name,
                    muscleGroup = muscleGroup,
                    equipment = equipment,
                    defaultRestSeconds = defaultRestSeconds,
                    initialPosition = initialPosition,
                    execution = execution,
                    commonMistakes = commonMistakes
                )
                showCreateDialog = false
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
            ExerciseThumbnail(
                thumbnailUrl = exercise.thumbnailUrl,
                gifUrl = exercise.gifUrl,
                primaryMuscleGroup = exercise.primaryMuscleGroup,
                equipment = exercise.equipment,
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                contentDescription = exercise.name
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exercise.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (exercise.isExcluded) GymTextSecondary else GymTextPrimary
                    )
                    if (exercise.isCustom) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⭐",
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                val subtitle = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = if (exercise.isExcluded) GymTextSecondary else GymAccentGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(exercise.primaryMuscleGroup.displayNameRu)
                    }
                    withStyle(SpanStyle(color = GymTextSecondary)) {
                        append(" • ${exercise.equipment.displayNameRu}")
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

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
