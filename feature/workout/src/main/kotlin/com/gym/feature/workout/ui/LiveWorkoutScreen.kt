package com.gym.feature.workout.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
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
                onFinishClick = {
                    viewModel.finishWorkout(current.workout.id)
                    onFinishClick()
                },
                onAddSet = { exerciseId, count ->
                    viewModel.addSetToExercise(exerciseId, count)
                },
                onAddExerciseClick = onAddExerciseClick,
                onAttachVideo = { setId, uri ->
                    viewModel.attachVideoToSet(setId, uri)
                },
                onToggleSet = { setId, isCompleted ->
                    viewModel.toggleSetCompletion(setId, isCompleted)
                },
                onSelectEffort = { setId, effort ->
                    viewModel.updateSetEffort(setId, effort)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutContent(
    workout: Workout,
    onFinishClick: () -> Unit,
    onAddSet: (Long, Int) -> Unit,
    onAddExerciseClick: () -> Unit = {},
    onAttachVideo: (Long, String) -> Unit = { _, _ -> },
    onToggleSet: (Long, Boolean) -> Unit = { _, _ -> },
    onSelectEffort: (Long, PerceivedEffort) -> Unit = { _, _ -> }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = workout.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymTextPrimary
                        )
                        Text(
                            text = "Объем: ${workout.totalVolume.toInt()} кг • Подходов: ${workout.totalCompletedSets}",
                            fontSize = 12.sp,
                            color = GymAccentGreen
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(workout.exercises, key = { it.id }) { exercise ->
                WorkoutExerciseCard(
                    exercise = exercise,
                    onAddSetClick = { onAddSet(exercise.id, exercise.sets.size) },
                    onAttachVideo = onAttachVideo,
                    onToggleSet = onToggleSet,
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
                        text = "Добавить упражнение в тренировку",
                        color = GymTextPrimary,
                        fontWeight = FontWeight.Bold
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
        Column(modifier = Modifier.padding(16.dp)) {
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
                        text = exercise.exercise.primaryMuscleGroup.displayNameRu,
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("СЕТ", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.width(36.dp))
                Text("ВЕС (КГ)", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.weight(1f))
                Text("ПОВТОРЫ", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.weight(1f))
                Text("ВИДЕО", fontSize = 11.sp, color = GymTextSecondary, modifier = Modifier.width(40.dp))
                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            exercise.sets.forEach { set ->
                WorkoutSetRow(
                    set = set,
                    onAttachVideo = { uri -> onAttachVideo(set.id, uri) },
                    onToggleCompleted = { onToggleSet(set.id, set.isCompleted) },
                    onSelectEffort = { effort -> onSelectEffort(set.id, effort) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddSetClick,
                colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
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
                .clip(RoundedCornerShape(8.dp))
                .background(if (set.isCompleted) GymAccentGreen.copy(alpha = 0.1f) else Color.Transparent)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SetTypeBadge(setNumber = set.setNumber, setType = set.setType)

            Text(
                text = "${set.weight} кг",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = GymTextPrimary,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )

            Text(
                text = "${set.reps}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = GymTextPrimary,
                modifier = Modifier.weight(1f)
            )

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
                    contentDescription = "Записать видео подхода для тренера (до 60 сек)",
                    tint = if (currentVideoUri != null) GymAccentGreen else GymTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Крупная кнопка завершения сета (Ergonomics First)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
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

        // Шкала усилия (RPE / RIR) появляется сразу после завершения сета
        if (set.isCompleted) {
            Spacer(modifier = Modifier.height(4.dp))
            PerceivedEffortSelector(
                selectedEffort = set.effort,
                onEffortSelect = onSelectEffort
            )
        }
    }
}
