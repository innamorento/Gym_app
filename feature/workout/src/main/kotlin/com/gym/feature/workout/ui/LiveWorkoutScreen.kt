package com.gym.feature.workout.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.PerceivedEffort
import com.gym.core.model.Workout
import com.gym.core.model.WorkoutExercise
import com.gym.core.model.WorkoutSet
import com.gym.core.ui.component.PerceivedEffortSelector
import com.gym.core.ui.component.SetTypeBadge
import com.gym.core.ui.component.SupersetBadge
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentPurple
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import com.gym.feature.workout.RestTimerState
import com.gym.feature.workout.WorkoutUiState
import com.gym.feature.workout.WorkoutViewModel
import com.gym.feature.workout.video.VideoRecordingHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWorkoutScreen(
    viewModel: WorkoutViewModel,
    onFinishClick: () -> Unit,
    onAddExerciseClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val elapsedSeconds by viewModel.workoutDurationSeconds.collectAsState()
    val restTimer by viewModel.restTimerState.collectAsState()

    var editingSet by remember { mutableStateOf<WorkoutSet?>(null) }

    when (val current = state) {
        is WorkoutUiState.Idle -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GymDarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет активной тренировки", color = GymTextSecondary)
            }
        }
        is WorkoutUiState.Active -> {
            ActiveWorkoutContent(
                workout = current.workout,
                elapsedSeconds = elapsedSeconds,
                restTimer = restTimer,
                onFinishClick = {
                    viewModel.finishWorkout(current.workout.id)
                    onFinishClick()
                },
                onAddSet = { exerciseId, count ->
                    viewModel.addSetToExercise(exerciseId, count)
                },
                onAddExerciseClick = onAddExerciseClick,
                onEditSet = { set -> editingSet = set },
                onAttachVideo = { setId, uri ->
                    viewModel.attachVideoToSet(setId, uri)
                },
                onToggleSet = { setId, isCompleted, restSec ->
                    viewModel.toggleSetCompletion(setId, isCompleted, restSec)
                },
                onSelectEffort = { setId, effort ->
                    viewModel.updateSetEffort(setId, effort)
                },
                onAdjustRestTimer = { delta -> viewModel.adjustRestTimer(delta) },
                onSetRestTimer = { sec -> viewModel.setRestTimer(sec) },
                onSkipRestTimer = { viewModel.stopRestTimer() }
            )

            // Диалог редактирования веса и повторений
            if (editingSet != null) {
                SetEditDialog(
                    set = editingSet!!,
                    onDismiss = { editingSet = null },
                    onSave = { weight, reps ->
                        viewModel.updateSetWeightAndReps(editingSet!!.id, weight, reps)
                        editingSet = null
                    },
                    onDelete = {
                        viewModel.removeSet(editingSet!!.id)
                        editingSet = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutContent(
    workout: Workout,
    elapsedSeconds: Long,
    restTimer: RestTimerState,
    onFinishClick: () -> Unit,
    onAddSet: (Long, Int) -> Unit,
    onAddExerciseClick: () -> Unit = {},
    onEditSet: (WorkoutSet) -> Unit = {},
    onAttachVideo: (Long, String) -> Unit = { _, _ -> },
    onToggleSet: (Long, Boolean, Int) -> Unit = { _, _, _ -> },
    onSelectEffort: (Long, PerceivedEffort) -> Unit = { _, _ -> },
    onAdjustRestTimer: (Int) -> Unit = {},
    onSetRestTimer: (Int) -> Unit = {},
    onSkipRestTimer: () -> Unit = {}
) {
    // Форматирование секундомера тренировки (ЧЧ:ММ:СС или ММ:СС)
    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val durationFormatted = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = workout.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱ $durationFormatted",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymAccentBlue
                            )
                            Text(
                                text = " • ${workout.totalVolume.toInt()} кг • Сеты: ${workout.totalCompletedSets}",
                                fontSize = 12.sp,
                                color = GymAccentGreen
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = onFinishClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
                    ) {
                        Text("Завершить", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymDarkBackground)
            )
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Плашка таймера отдыха между подходами (Rest Timer)
            item {
                AnimatedVisibility(
                    visible = restTimer.isRunning || restTimer.remainingSeconds > 0,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    RestTimerBanner(
                        timerState = restTimer,
                        onAdjust = onAdjustRestTimer,
                        onSetTime = onSetRestTimer,
                        onSkip = onSkipRestTimer
                    )
                }
            }

            items(workout.exercises, key = { it.id }) { exercise ->
                WorkoutExerciseCard(
                    exercise = exercise,
                    onAddSetClick = { onAddSet(exercise.id, exercise.sets.size) },
                    onEditSet = onEditSet,
                    onAttachVideo = onAttachVideo,
                    onToggleSet = { setId, isCompleted ->
                        onToggleSet(setId, isCompleted, exercise.exercise.defaultRestSeconds)
                    },
                    onSelectEffort = onSelectEffort
                )
            }

            item {
                Button(
                    onClick = onAddExerciseClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GymAccentGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Добавить упражнение в тренировку",
                        color = GymTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Баннер таймера отдыха с гибким редактированием времени (+15с, -15с, пресеты)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestTimerBanner(
    timerState: RestTimerState,
    onAdjust: (Int) -> Unit,
    onSetTime: (Int) -> Unit,
    onSkip: () -> Unit
) {
    val mins = timerState.remainingSeconds / 60
    val secs = timerState.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)
    val progress = if (timerState.totalSeconds > 0) {
        timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymAccentBlue.copy(alpha = 0.5f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = GymAccentBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Отдых между подходами:", fontSize = 13.sp, color = GymTextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onSkip, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Пропустить", tint = GymTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GymAccentBlue,
                trackColor = GymDarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Кнопки быстрой корректировки времени отдыха
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = false,
                    onClick = { onAdjust(-15) },
                    label = { Text("-15с", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = GymDarkSurfaceVariant, labelColor = GymTextPrimary)
                )
                FilterChip(
                    selected = false,
                    onClick = { onAdjust(30) },
                    label = { Text("+30с", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = GymDarkSurfaceVariant, labelColor = GymTextPrimary)
                )
                listOf(60, 90, 120, 180).forEach { sec ->
                    FilterChip(
                        selected = timerState.totalSeconds == sec,
                        onClick = { onSetTime(sec) },
                        label = { Text("${sec}с", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymAccentBlue,
                            selectedLabelColor = GymDarkBackground,
                            containerColor = GymDarkSurfaceVariant,
                            labelColor = GymTextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutExerciseCard(
    exercise: WorkoutExercise,
    onAddSetClick: () -> Unit,
    onEditSet: (WorkoutSet) -> Unit,
    onAttachVideo: (Long, String) -> Unit,
    onToggleSet: (Long, Boolean) -> Unit,
    onSelectEffort: (Long, PerceivedEffort) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (exercise.isPartOfSuperset) {
                    Modifier.border(1.dp, GymAccentPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = exercise.exercise.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = "${exercise.exercise.primaryMuscleGroup.displayNameRu} • ${exercise.exercise.equipment.displayNameRu} (отдых ${exercise.exercise.defaultRestSeconds}с)",
                        fontSize = 12.sp,
                        color = GymTextSecondary
                    )
                }

                if (exercise.isPartOfSuperset) {
                    SupersetBadge(groupLabel = "Суперсет")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Таблица подходов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("СЕТ", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.width(36.dp))
                Text("ВЕС", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.weight(1f).padding(start = 6.dp))
                Text("ПОВТОРЫ", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.weight(1f))
                Text("ВИДЕО", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.width(36.dp))
                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            exercise.sets.forEach { set ->
                WorkoutSetRow(
                    set = set,
                    onEditClick = { onEditSet(set) },
                    onAttachVideo = { uri -> onAttachVideo(set.id, uri) },
                    onToggleCompleted = { onToggleSet(set.id, set.isCompleted) },
                    onSelectEffort = { effort -> onSelectEffort(set.id, effort) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onAddSetClick,
                colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = GymAccentGreen)
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Добавить подход", color = GymTextPrimary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun WorkoutSetRow(
    set: WorkoutSet,
    onEditClick: () -> Unit = {},
    onAttachVideo: (String) -> Unit = {},
    onToggleCompleted: () -> Unit = {},
    onSelectEffort: (PerceivedEffort) -> Unit = {}
) {
    val context = LocalContext.current
    var currentVideoUri by remember { mutableStateOf<String?>(null) }
    var tempCaptureUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && tempCaptureUri != null) {
            val uriStr = tempCaptureUri.toString()
            currentVideoUri = uriStr
            onAttachVideo(uriStr)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (set.isCompleted) GymAccentGreen.copy(alpha = 0.1f) else Color.Transparent)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SetTypeBadge(setNumber = set.setNumber, setType = set.setType)

            // Интерактивная плашка ввода веса
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GymDarkSurfaceVariant)
                    .clickable { onEditClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (set.weight > 0) "${set.weight} кг" else "0.0 кг",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (set.weight > 0) GymTextPrimary else GymTextSecondary
                )
            }

            // Интерактивная плашка ввода повторений
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GymDarkSurfaceVariant)
                    .clickable { onEditClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (set.reps > 0) "${set.reps} повт." else "0 повт.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (set.reps > 0) GymTextPrimary else GymTextSecondary
                )
            }

            IconButton(
                onClick = {
                    val (_, uri) = VideoRecordingHelper.createVideoFileUri(context, set.id)
                    tempCaptureUri = uri
                    videoLauncher.launch(uri)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Записать видео подхода для тренера",
                    tint = if (currentVideoUri != null) GymAccentGreen else GymTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Чекбокс завершения сета (Ergonomics First)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (set.isCompleted) GymAccentGreen else GymDarkSurfaceVariant)
                    .clickable { onToggleCompleted() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Завершить подход",
                    tint = if (set.isCompleted) GymDarkBackground else GymTextSecondary
                )
            }
        }

        // Шкала усилия (RPE / RIR)
        if (set.isCompleted) {
            Spacer(modifier = Modifier.height(4.dp))
            PerceivedEffortSelector(
                selectedEffort = set.effort,
                onEffortSelect = onSelectEffort
            )
        }
    }
}

/**
 * Диалог редактирования параметров подхода (Вес и Повторы) со степперами и прямым вводом
 */
@Composable
fun SetEditDialog(
    set: WorkoutSet,
    onDismiss: () -> Unit,
    onSave: (Double, Int) -> Unit,
    onDelete: () -> Unit
) {
    var weight by remember { mutableDoubleStateOf(set.weight) }
    var reps by remember { mutableIntStateOf(set.reps) }
    var weightText by remember { mutableStateOf(if (set.weight > 0) set.weight.toString() else "") }
    var repsText by remember { mutableStateOf(if (set.reps > 0) set.reps.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GymDarkSurface,
        title = {
            Text(
                text = "Подход #${set.setNumber}",
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Редактор веса
                Text("Вес снаряда (кг):", fontSize = 13.sp, color = GymTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            weight = (weight - 2.5).coerceAtLeast(0.0)
                            weightText = weight.toString()
                        }) {
                            Text("-2.5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GymTextPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = {
                            weightText = it
                            weight = it.toDoubleOrNull() ?: weight
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(130.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GymTextPrimary,
                            unfocusedTextColor = GymTextPrimary,
                            focusedBorderColor = GymAccentGreen,
                            unfocusedBorderColor = GymDarkSurfaceVariant
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            weight = (weight + 2.5).coerceAtLeast(0.0)
                            weightText = weight.toString()
                        }) {
                            Text("+2.5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GymAccentGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Редактор повторений
                Text("Количество повторений:", fontSize = 13.sp, color = GymTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            reps = (reps - 1).coerceAtLeast(0)
                            repsText = reps.toString()
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = GymTextPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = repsText,
                        onValueChange = {
                            repsText = it
                            reps = it.toIntOrNull() ?: reps
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(130.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GymTextPrimary,
                            unfocusedTextColor = GymTextPrimary,
                            focusedBorderColor = GymAccentGreen,
                            unfocusedBorderColor = GymDarkSurfaceVariant
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            reps += 1
                            repsText = reps.toString()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GymAccentGreen)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(weight, reps) },
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
            ) {
                Text("Сохранить", color = GymDarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Удалить подход", color = GymAccentRed)
                }
                TextButton(onClick = onDismiss) {
                    Text("Отмена", color = GymTextSecondary)
                }
            }
        }
    )
}
