package com.gym.feature.templates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.gym.feature.templates.TemplateEditorIntent
import com.gym.feature.templates.TemplateExerciseDraft
import com.gym.feature.templates.TemplatesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    templateId: Long,
    viewModel: TemplatesViewModel,
    onBackClick: () -> Unit,
    onAddExerciseClick: () -> Unit
) {
    val state by viewModel.editorUiState.collectAsState()
    var selectedForSuperset by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(templateId) {
        viewModel.initEditor(templateId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (templateId == 0L) "Новый шаблон" else "Редактирование",
                        color = GymTextPrimary,
                        fontWeight = FontWeight.Bold
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
                    IconButton(
                        onClick = { viewModel.handleIntent(TemplateEditorIntent.SaveTemplate) },
                        enabled = state.title.isNotBlank() && state.exercises.isNotEmpty() && !state.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить",
                            tint = if (state.title.isNotBlank() && state.exercises.isNotEmpty()) GymAccentGreen else GymTextSecondary
                        )
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
            item {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.handleIntent(TemplateEditorIntent.ChangeTitle(it)) },
                    label = { Text("Название шаблона (напр. 'День тяги')") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GymTextPrimary,
                        unfocusedTextColor = GymTextPrimary,
                        focusedBorderColor = GymAccentGreen,
                        unfocusedBorderColor = GymDarkSurfaceVariant,
                        focusedLabelColor = GymAccentGreen,
                        unfocusedLabelColor = GymTextSecondary
                    ),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Упражнения (${state.exercises.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )

                    Button(
                        onClick = onAddExerciseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = GymAccentGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить", color = GymTextPrimary)
                    }
                }
            }

            itemsIndexed(state.exercises, key = { _, item -> item.tempId }) { index, item ->
                TemplateExerciseItemView(
                    item = item,
                    isSelectedForSuperset = selectedForSuperset == item.tempId,
                    onSelectForSuperset = {
                        if (selectedForSuperset == null) {
                            selectedForSuperset = item.tempId
                        } else if (selectedForSuperset == item.tempId) {
                            selectedForSuperset = null
                        } else {
                            viewModel.handleIntent(
                                TemplateEditorIntent.LinkAsSuperset(
                                    firstTempId = selectedForSuperset!!,
                                    secondTempId = item.tempId
                                )
                            )
                            selectedForSuperset = null
                        }
                    },
                    onUnlinkSuperset = {
                        viewModel.handleIntent(TemplateEditorIntent.UnlinkFromSuperset(item.tempId))
                    },
                    onRemove = {
                        viewModel.handleIntent(TemplateEditorIntent.RemoveExercise(item.tempId))
                    }
                )
            }
        }
    }
}

@Composable
fun TemplateExerciseItemView(
    item: TemplateExerciseDraft,
    isSelectedForSuperset: Boolean,
    onSelectForSuperset: () -> Unit,
    onUnlinkSuperset: () -> Unit,
    onRemove: () -> Unit
) {
    val isSuperset = !item.supersetId.isNullOrBlank()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GymDarkSurface)
            .then(
                if (isSelectedForSuperset) {
                    Modifier.border(2.dp, GymAccentPurple, RoundedCornerShape(12.dp))
                } else if (isSuperset) {
                    Modifier.border(1.dp, GymAccentPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                } else Modifier
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.exercise.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = "${item.exercise.primaryMuscleGroup.displayNameRu} • ${item.exercise.equipment.displayNameRu}",
                        fontSize = 12.sp,
                        color = GymTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSuperset) {
                        IconButton(onClick = onUnlinkSuperset) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = "Отвязать суперсет",
                                tint = GymAccentPurple
                            )
                        }
                    } else {
                        IconButton(onClick = onSelectForSuperset) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Объединить в суперсет",
                                tint = if (isSelectedForSuperset) GymAccentPurple else GymTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Удалить",
                            tint = GymTextSecondary
                        )
                    }
                }
            }

            if (isSuperset) {
                Spacer(modifier = Modifier.height(6.dp))
                SupersetBadge(groupLabel = "Суперсет")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Подходов: ${item.targetSetsCount}",
                    fontSize = 13.sp,
                    color = GymTextSecondary
                )
                Text(
                    text = "Отдых: ${item.defaultRestSeconds} сек",
                    fontSize = 13.sp,
                    color = GymTextSecondary
                )
            }
        }
    }
}
