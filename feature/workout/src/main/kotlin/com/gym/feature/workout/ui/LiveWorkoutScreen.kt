package com.gym.feature.workout.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.Equipment
import com.gym.core.model.EquipmentWeightProfile
import com.gym.core.model.Exercise
import com.gym.core.model.MuscleGroup
import com.gym.core.ui.icon.GymIcons
import com.gym.core.model.PerceivedEffort
import com.gym.core.model.ProgressionDecision
import com.gym.core.model.ProgressionRecommendation
import com.gym.core.model.Workout
import com.gym.core.model.WorkoutExercise
import com.gym.core.model.WorkoutSet
import com.gym.core.ui.component.ExerciseDetailSheet
import com.gym.core.ui.component.ExerciseThumbnail
import com.gym.core.ui.component.PerceivedEffortSelector
import com.gym.core.ui.component.PlateCalculatorDialog
import com.gym.core.ui.component.SetTypeBadge
import com.gym.core.ui.component.SupersetBadge
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentGreenVibrant
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
    onFinishClick: (Long) -> Unit,
    onDiscardClick: () -> Unit = {},
    onAddExerciseClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val elapsedSeconds by viewModel.workoutDurationSeconds.collectAsState()
    val restTimer by viewModel.restTimerState.collectAsState()
    val recommendations by viewModel.progressionRecommendations.collectAsState()
    val isAutoProgression by viewModel.isAutoProgressionEnabled.collectAsState()
    val equipmentProfiles by viewModel.equipmentProfiles.collectAsState()
    val allActiveExercises by viewModel.allActiveExercises.collectAsState()

    var editingSet by remember { mutableStateOf<WorkoutSet?>(null) }
    var selectedExerciseForDetail by remember { mutableStateOf<Exercise?>(null) }
    var replacingWorkoutExercise by remember { mutableStateOf<WorkoutExercise?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showPlateCalculator by remember { mutableStateOf(false) }
    var plateCalcInitialWeight by remember { mutableDoubleStateOf(60.0) }

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
                recommendations = recommendations,
                isAutoProgression = isAutoProgression,
                onToggleAutoProgression = { viewModel.toggleAutoProgression() },
                onApplyRecommendation = { exId, rec -> viewModel.applyProgressionRecommendation(exId, rec) },
                onFinishClick = {
                    viewModel.finishWorkout(current.workout.id) { finishedId ->
                        onFinishClick(finishedId)
                    }
                },
                onDiscardClick = { showDiscardDialog = true },
                onOpenPlateCalculator = {
                    val firstSetWeight = current.workout.exercises.firstOrNull()?.sets?.firstOrNull()?.weight ?: 60.0
                    plateCalcInitialWeight = if (firstSetWeight > 0.0) firstSetWeight else 60.0
                    showPlateCalculator = true
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
                onSkipRestTimer = { viewModel.stopRestTimer() },
                onExerciseClick = { exercise -> selectedExerciseForDetail = exercise },
                onReplaceExerciseClick = { ex -> replacingWorkoutExercise = ex },
                onDeleteExerciseClick = { exId -> viewModel.removeExerciseFromActiveWorkout(exId) }
            )

            // Диалог отмены / сброса активной тренировки
            if (showDiscardDialog) {
                AlertDialog(
                    onDismissRequest = { showDiscardDialog = false },
                    containerColor = GymDarkSurface,
                    title = {
                        Text("Отменить тренировку?", fontWeight = FontWeight.Bold, color = GymTextPrimary)
                    },
                    text = {
                        Text(
                            text = "Все подходы и данные текущей сессии будут удалены без сохранения.",
                            color = GymTextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDiscardDialog = false
                                viewModel.discardWorkout(current.workout.id)
                                onDiscardClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GymAccentRed)
                        ) {
                            Text("Отменить сессию", color = GymTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDiscardDialog = false }) {
                            Text("Продолжить", color = GymTextSecondary)
                        }
                    }
                )
            }

            // Калькулятор блинов на гриф
            if (showPlateCalculator) {
                PlateCalculatorDialog(
                    initialTargetWeight = plateCalcInitialWeight,
                    onDismiss = { showPlateCalculator = false },
                    onApplyWeight = { showPlateCalculator = false }
                )
            }

            // Детальная карточка упражнения с GIF-анимацией и техникой выполнения
            val detailExercise = selectedExerciseForDetail
            if (detailExercise != null) {
                ExerciseDetailSheet(
                    exercise = detailExercise,
                    onDismiss = { selectedExerciseForDetail = null },
                    onSelectExercise = { selectedExerciseForDetail = null }
                )
            }

            // Диалог быстрой замены упражнения из той же мышечной группы
            val replacingEx = replacingWorkoutExercise
            if (replacingEx != null) {
                ReplaceExerciseDialog(
                    currentExercise = replacingEx.exercise,
                    allExercises = allActiveExercises,
                    onDismiss = { replacingWorkoutExercise = null },
                    onSelect = { newExercise ->
                        viewModel.replaceExerciseInActiveWorkout(replacingEx.id, newExercise)
                        replacingWorkoutExercise = null
                    }
                )
            }

            // Диалог редактирования веса и повторений
            if (editingSet != null) {
                val exerciseForSet = current.workout.exercises.find { ex -> ex.sets.any { it.id == editingSet?.id } }
                val equipment = exerciseForSet?.exercise?.equipment ?: Equipment.BARBELL
                val profile = equipmentProfiles[equipment]
                SetEditDialog(
                    set = editingSet!!,
                    equipment = equipment,
                    equipmentProfile = profile,
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
    recommendations: Map<Long, ProgressionRecommendation> = emptyMap(),
    isAutoProgression: Boolean = false,
    onToggleAutoProgression: () -> Unit = {},
    onApplyRecommendation: (Long, ProgressionRecommendation) -> Unit = { _, _ -> },
    onFinishClick: () -> Unit,
    onDiscardClick: () -> Unit = {},
    onOpenPlateCalculator: () -> Unit = {},
    onAddSet: (Long, Int) -> Unit,
    onAddExerciseClick: () -> Unit = {},
    onEditSet: (WorkoutSet) -> Unit = {},
    onAttachVideo: (Long, String) -> Unit = { _, _ -> },
    onToggleSet: (Long, Boolean, Int) -> Unit = { _, _, _ -> },
    onSelectEffort: (Long, PerceivedEffort) -> Unit = { _, _ -> },
    onAdjustRestTimer: (Int) -> Unit = {},
    onSetRestTimer: (Int) -> Unit = {},
    onSkipRestTimer: () -> Unit = {},
    onExerciseClick: (Exercise) -> Unit = {},
    onReplaceExerciseClick: (WorkoutExercise) -> Unit = {},
    onDeleteExerciseClick: (Long) -> Unit = {}
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
                                color = GymAccentBlue,
                                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
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
                    IconButton(
                        onClick = onOpenPlateCalculator,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            painter = painterResource(GymIcons.PlateCalc),
                            contentDescription = "Калькулятор блинов",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = onDiscardClick,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Отменить тренировку",
                            tint = GymTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
        bottomBar = {
            // Плавающий таймер отдыха всегда перед глазами внизу экрана
            AnimatedVisibility(
                visible = restTimer.isRunning || restTimer.remainingSeconds > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = GymDarkSurface,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        RestTimerBanner(
                            timerState = restTimer,
                            onAdjust = onAdjustRestTimer,
                            onSetTime = onSetRestTimer,
                            onSkip = onSkipRestTimer
                        )
                    }
                }
            }
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
            // Переключатель режима "Следовать подсказкам ИИ" (Авто-прогрессия нагрузки)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAutoProgression) GymAccentGreen.copy(alpha = 0.15f) else GymDarkSurfaceVariant.copy(alpha = 0.6f))
                        .clickable { onToggleAutoProgression() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(GymIcons.Bolt),
                            contentDescription = null,
                            tint = if (isAutoProgression) GymAccentGreen else GymTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAutoProgression) "ИИ Авто-прогрессия активна" else "Следовать подсказкам ИИ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isAutoProgression) GymAccentGreen else GymTextPrimary
                        )
                    }
                    Switch(
                        checked = isAutoProgression,
                        onCheckedChange = { onToggleAutoProgression() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GymDarkBackground,
                            checkedTrackColor = GymAccentGreen,
                            uncheckedThumbColor = GymTextSecondary,
                            uncheckedTrackColor = GymDarkSurface
                        )
                    )
                }
            }

            items(workout.exercises, key = { it.id }) { exercise ->
                val rec = recommendations[exercise.id]
                WorkoutExerciseCard(
                    recommendation = rec,
                    isAutoProgression = isAutoProgression,
                    onApplyRecommendation = { onApplyRecommendation(exercise.id, it) },
                    exercise = exercise,
                    onExerciseClick = onExerciseClick,
                    onAddSetClick = { onAddSet(exercise.id, exercise.sets.size) },
                    onEditSet = onEditSet,
                    onAttachVideo = onAttachVideo,
                    onToggleSet = { setId, isCompleted ->
                        onToggleSet(setId, isCompleted, exercise.exercise.defaultRestSeconds)
                    },
                    onSelectEffort = onSelectEffort,
                    onReplaceExerciseClick = { onReplaceExerciseClick(exercise) },
                    onDeleteExerciseClick = { onDeleteExerciseClick(exercise.id) }
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
                    Icon(painter = painterResource(GymIcons.Timer), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Отдых между подходами:", fontSize = 13.sp, color = GymTextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentBlue,
                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onSkip, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Пропустить",
                            tint = GymTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
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
    recommendation: ProgressionRecommendation? = null,
    isAutoProgression: Boolean = false,
    onApplyRecommendation: (ProgressionRecommendation) -> Unit = {},
    onExerciseClick: (Exercise) -> Unit = {},
    onAddSetClick: () -> Unit,
    onEditSet: (WorkoutSet) -> Unit,
    onAttachVideo: (Long, String) -> Unit,
    onToggleSet: (Long, Boolean) -> Unit,
    onSelectEffort: (Long, PerceivedEffort) -> Unit,
    onReplaceExerciseClick: () -> Unit = {},
    onDeleteExerciseClick: () -> Unit = {}
) {
    val allCompleted = exercise.sets.isNotEmpty() && exercise.sets.all { it.isCompleted }
    // [START:TASK-017] Auto-collapse card only after full input: isCompleted, reps > 0, effort != null
    val isFullyLogged = exercise.sets.isNotEmpty() && exercise.sets.all { 
        it.isCompleted && it.reps > 0 && it.effort != null 
    }
    var isManuallyExpanded by rememberSaveable(exercise.id) { mutableStateOf<Boolean?>(null) }
    // [AI-TASK:TASK-022] When all sets are completed and logged, reset isManuallyExpanded so card auto-collapses
    LaunchedEffect(isFullyLogged) {
        if (isFullyLogged) {
            isManuallyExpanded = false
        }
    }
    val isExpanded = isManuallyExpanded ?: !isFullyLogged
    // [END:TASK-017]
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (exercise.isPartOfSuperset) {
                    Modifier.border(1.dp, GymAccentPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullyLogged && !isExpanded) GymDarkSurface.copy(alpha = 0.65f) else GymDarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        if (!isExpanded) {
                            isManuallyExpanded = true
                        } else {
                            onExerciseClick(exercise.exercise)
                        }
                    }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFullyLogged && !isExpanded) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GymAccentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Завершено",
                                tint = GymAccentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        ExerciseThumbnail(
                            thumbnailUrl = exercise.exercise.thumbnailUrl,
                            gifUrl = exercise.exercise.gifUrl,
                            primaryMuscleGroup = exercise.exercise.primaryMuscleGroup,
                            equipment = exercise.exercise.equipment,
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentDescription = exercise.exercise.name
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = exercise.exercise.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFullyLogged && !isExpanded) GymTextSecondary else GymTextPrimary
                            )
                            if (isExpanded) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Инструкция и техника",
                                    tint = GymAccentGreen.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (isFullyLogged && !isExpanded) {
                            Text(
                                text = "✓ Все подходы выполнены (${exercise.sets.size}/${exercise.sets.size})",
                                fontSize = 12.sp,
                                color = GymAccentGreen
                            )
                        } else {
                            Text(
                                text = "${exercise.exercise.primaryMuscleGroup.displayNameRu} • ${exercise.exercise.equipment.displayNameRu} (отдых ${exercise.exercise.defaultRestSeconds}с)",
                                fontSize = 12.sp,
                                color = GymTextSecondary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (exercise.isPartOfSuperset) {
                        SupersetBadge(groupLabel = "Суперсет")
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Меню дополнительных действий (Заменить / Удалить)
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Опции",
                                tint = GymTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(GymDarkSurfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Заменить упражнение", color = GymTextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    showMenu = false
                                    onReplaceExerciseClick()
                                },
                                leadingIcon = {
                                    Icon(painter = painterResource(GymIcons.Swap), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить из тренировки", color = GymAccentRed, fontSize = 13.sp) },
                                onClick = {
                                    showMenu = false
                                    onDeleteExerciseClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = GymAccentRed, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }

                    // Кнопка свернуть / развернуть
                    IconButton(
                        onClick = { isManuallyExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                            tint = GymTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (recommendation != null) {
                        SmartProgressionBanner(
                            recommendation = recommendation,
                            isAutoProgression = isAutoProgression,
                            onApplyClick = { onApplyRecommendation(recommendation) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Таблица подходов
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("СЕТ", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.width(36.dp))
                        Text("ВЕС", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.weight(1f).padding(start = 6.dp))
                        Text("ПОВТОРЫ", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(GymIcons.Videocam),
                                contentDescription = "Видео",
                                modifier = Modifier.size(16.dp),
                                tint = GymTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(48.dp))
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

            // Интерактивная плашка ввода веса (Touch Target >= 48dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymDarkSurfaceVariant)
                    .clickable { onEditClick() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (set.weight > 0) "${set.weight} кг" else "0.0 кг",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (set.weight > 0) GymTextPrimary else GymTextSecondary,
                    style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                )
            }

            // Интерактивная плашка ввода повторений (Touch Target >= 48dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymDarkSurfaceVariant)
                    .clickable { onEditClick() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (set.reps > 0) "${set.reps} повт." else "0 повт.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (set.reps > 0) GymTextPrimary else GymTextSecondary,
                    style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                )
            }

            IconButton(
                onClick = {
                    val (_, uri) = VideoRecordingHelper.createVideoFileUri(context, set.id)
                    tempCaptureUri = uri
                    videoLauncher.launch(uri)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (currentVideoUri != null) {
                            Modifier.border(1.dp, GymAccentGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(GymAccentGreen.copy(alpha = 0.12f))
                        } else Modifier
                    )
            ) {
                Icon(
                    painter = painterResource(GymIcons.Videocam),
                    contentDescription = "Записать видео подхода для тренера",
                    tint = if (currentVideoUri != null) GymAccentGreen else Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Чекбокс завершения сета (Ergonomics First: Touch Target >= 48dp)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (set.isCompleted) GymAccentGreen.copy(alpha = 0.2f) else GymDarkSurfaceVariant)
                    .border(
                        width = if (set.isCompleted) 1.5.dp else 1.dp,
                        color = if (set.isCompleted) GymAccentGreen else Color(0xFF2E3545),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onToggleCompleted() },
                contentAlignment = Alignment.Center
            ) {
                if (set.isCompleted) {
                    Icon(
                        painter = painterResource(GymIcons.CheckHex),
                        contentDescription = "Завершить подход",
                        modifier = Modifier.size(34.dp),
                        tint = Color.Unspecified
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.5.dp, Color(0xFF5A6679), RoundedCornerShape(6.dp))
                    )
                }
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
 * Диалог редактирования параметров подхода (Вес и Повторы) с динамическим шагом снаряда из настроек
 */
@Composable
fun SetEditDialog(
    set: WorkoutSet,
    equipment: Equipment = Equipment.BARBELL,
    equipmentProfile: EquipmentWeightProfile? = null,
    onDismiss: () -> Unit,
    onSave: (Double, Int) -> Unit,
    onDelete: () -> Unit
) {
    var weight by remember { mutableDoubleStateOf(set.weight) }
    var reps by remember { mutableIntStateOf(set.reps) }
    var weightText by remember { mutableStateOf(if (set.weight > 0) (if (set.weight % 1.0 == 0.0) set.weight.toInt().toString() else set.weight.toString()) else "") }
    var repsText by remember { mutableStateOf(if (set.reps > 0) set.reps.toString() else "") }

    val step = equipmentProfile?.getStepForWeight(weight) ?: when (equipment) {
        Equipment.DUMBBELL -> 1.0
        Equipment.BARBELL -> 2.5
        Equipment.CABLE, Equipment.MACHINE -> 2.5
        else -> 1.0
    }
    val stepLabel = if (step % 1.0 == 0.0) step.toInt().toString() else step.toString()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GymDarkSurface,
        title = {
            Text(
                text = "Подход #${set.setNumber} (${equipment.displayNameRu})",
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Редактор веса (Touch Targets >= 48dp) с динамическим шагом
                Text("Вес снаряда (кг) [шаг: $stepLabel кг]:", fontSize = 13.sp, color = GymTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                weight = (weight - step).coerceAtLeast(0.0)
                                weightText = if (weight % 1.0 == 0.0) weight.toInt().toString() else weight.toString()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("-$stepLabel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GymTextPrimary)
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                weight = (weight + step).coerceAtLeast(0.0)
                                weightText = if (weight % 1.0 == 0.0) weight.toInt().toString() else weight.toString()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("+$stepLabel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GymAccentGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Редактор повторений (Touch Targets >= 48dp)
                Text("Количество повторений:", fontSize = 13.sp, color = GymTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                reps = (reps - 1).coerceAtLeast(0)
                                repsText = reps.toString()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = GymTextPrimary, modifier = Modifier.size(24.dp))
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                reps += 1
                                repsText = reps.toString()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GymAccentGreen, modifier = Modifier.size(24.dp))
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

@Composable
fun SmartProgressionBanner(
    recommendation: ProgressionRecommendation,
    isAutoProgression: Boolean = false,
    onApplyClick: () -> Unit
) {
    val borderColor = when (recommendation.decision) {
        ProgressionDecision.INCREASE_WEIGHT -> GymAccentGreen
        ProgressionDecision.DECREASE_WEIGHT -> Color(0xFFF59E0B)
        ProgressionDecision.MAINTAIN_WEIGHT -> GymDarkSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(GymIcons.AiBulb),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Рекомендация ИИ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (recommendation.goalTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val targetGoal = recommendation.goal
                            if (targetGoal != null) {
                                Icon(
                                    painter = painterResource(id = GymIcons.iconForGoal(targetGoal)),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = recommendation.goalTitle,
                                fontSize = 11.sp,
                                color = GymTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isAutoProgression) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GymAccentGreen.copy(alpha = 0.2f))
                            .border(1.dp, GymAccentGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ Применено ИИ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreenVibrant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Button(
                        onClick = onApplyClick,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (recommendation.decision == ProgressionDecision.INCREASE_WEIGHT) GymAccentGreen else GymDarkSurfaceVariant,
                            contentColor = if (recommendation.decision == ProgressionDecision.INCREASE_WEIGHT) GymDarkBackground else GymTextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Применить",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${recommendation.suggestedWeight} кг • ${recommendation.suggestedReps} повт.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = recommendation.rationaleRu,
                fontSize = 11.sp,
                color = GymTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Диалог быстрой замены упражнения в текущей тренировке с автоматическим предфильтром по мышечной группе
 */
@Composable
fun ReplaceExerciseDialog(
    currentExercise: Exercise,
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSelect: (Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterSameMuscleOnly by remember { mutableStateOf(true) }

    val filteredList = remember(searchQuery, filterSameMuscleOnly, allExercises, currentExercise) {
        allExercises.filter { ex ->
            ex.id != currentExercise.id &&
            (!filterSameMuscleOnly || ex.primaryMuscleGroup == currentExercise.primaryMuscleGroup) &&
            (searchQuery.isBlank() || ex.name.contains(searchQuery, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GymDarkSurface,
        title = {
            Column {
                Text(
                    text = "Заменить упражнение",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GymTextPrimary
                )
                Text(
                    text = "Текущее: ${currentExercise.name} (${currentExercise.primaryMuscleGroup.displayNameRu})",
                    fontSize = 12.sp,
                    color = GymTextSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterSameMuscleOnly,
                        onClick = { filterSameMuscleOnly = true },
                        label = { Text(currentExercise.primaryMuscleGroup.displayNameRu, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymAccentGreen,
                            selectedLabelColor = GymDarkBackground,
                            containerColor = GymDarkSurfaceVariant,
                            labelColor = GymTextSecondary
                        )
                    )
                    FilterChip(
                        selected = !filterSameMuscleOnly,
                        onClick = { filterSameMuscleOnly = false },
                        label = { Text("Все группы", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymAccentGreen,
                            selectedLabelColor = GymDarkBackground,
                            containerColor = GymDarkSurfaceVariant,
                            labelColor = GymTextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск упражнения...", color = GymTextSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GymTextPrimary,
                        unfocusedTextColor = GymTextPrimary,
                        focusedBorderColor = GymAccentGreen,
                        unfocusedBorderColor = GymDarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Упражнения не найдены", color = GymTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredList, key = { it.id }) { ex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GymDarkSurfaceVariant.copy(alpha = 0.5f))
                                    .clickable { onSelect(ex) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ExerciseThumbnail(
                                    thumbnailUrl = ex.thumbnailUrl,
                                    gifUrl = ex.gifUrl,
                                    primaryMuscleGroup = ex.primaryMuscleGroup,
                                    equipment = ex.equipment,
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentDescription = ex.name
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ex.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GymTextPrimary
                                    )
                                    Text(
                                        text = "${ex.primaryMuscleGroup.displayNameRu} • ${ex.equipment.displayNameRu}",
                                        fontSize = 11.sp,
                                        color = GymTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = GymTextSecondary)
            }
        }
    )
}