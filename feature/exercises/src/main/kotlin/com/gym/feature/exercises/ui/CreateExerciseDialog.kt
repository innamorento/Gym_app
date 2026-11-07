package com.gym.feature.exercises.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.gym.core.model.Equipment
import com.gym.core.model.MuscleGroup
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateExerciseDialog(
    onDismiss: () -> Unit,
    onCreateExercise: (
        name: String,
        muscleGroup: MuscleGroup,
        equipment: Equipment,
        defaultRestSeconds: Int,
        initialPosition: String?,
        execution: String?,
        commonMistakes: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf(MuscleGroup.CHEST) }
    var selectedEquipment by remember { mutableStateOf(Equipment.BARBELL) }
    var restSeconds by remember { mutableIntStateOf(90) }
    var initialPosition by remember { mutableStateOf("") }
    var execution by remember { mutableStateOf("") }
    var commonMistakes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GymDarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Новое упражнение",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = "Добавление в персональный каталог зала",
                        fontSize = 12.sp,
                        color = GymTextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = GymTextSecondary)
                }
            }

            // Название упражнения
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Название упражнения *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GymTextPrimary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Например: Жим гантелей на наклонной скамье", color = GymTextSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GymTextPrimary,
                        unfocusedTextColor = GymTextPrimary,
                        focusedBorderColor = GymAccentGreen,
                        unfocusedBorderColor = GymDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Целевая мышечная группа
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Основная группа мышц:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GymTextPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MuscleGroup.values().forEach { group ->
                        val isSelected = group == selectedMuscleGroup
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMuscleGroup = group },
                            label = { Text(group.displayNameRu, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentGreen,
                                selectedLabelColor = GymDarkBackground,
                                containerColor = GymDarkSurfaceVariant,
                                labelColor = GymTextSecondary
                            )
                        )
                    }
                }
            }

            // Тип используемого снаряда
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Тип снаряда / Оборудование:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GymTextPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Equipment.BARBELL,
                        Equipment.DUMBBELL,
                        Equipment.MACHINE,
                        Equipment.CABLE,
                        Equipment.BODYWEIGHT,
                        Equipment.WEIGHTED_BODYWEIGHT,
                        Equipment.KETTLEBELL,
                        Equipment.RESISTANCE_BAND
                    ).forEach { eq ->
                        val isSelected = eq == selectedEquipment
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedEquipment = eq },
                            label = { Text(eq.displayNameRu, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentBlue,
                                selectedLabelColor = Color.Black,
                                containerColor = GymDarkSurfaceVariant,
                                labelColor = GymTextSecondary
                            )
                        )
                    }
                }
            }

            // Время отдыха по умолчанию
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Время отдыха по умолчанию:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GymTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (restSeconds > 15) restSeconds -= 15 },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymDarkSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-15с", tint = GymTextPrimary)
                        }

                        Text(
                            text = "${restSeconds} сек",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreen,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { restSeconds += 15 },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymDarkSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+15с", tint = GymTextPrimary)
                        }
                    }

                    // Быстрые пресеты отдыха
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(60, 90, 120, 180).forEach { sec ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (restSeconds == sec) GymAccentGreen else GymDarkSurfaceVariant)
                                    .clickable { restSeconds = sec }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${sec}с",
                                    fontSize = 11.sp,
                                    fontWeight = if (restSeconds == sec) FontWeight.Bold else FontWeight.Medium,
                                    color = if (restSeconds == sec) GymDarkBackground else GymTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Пошаговая биомеханика и заметки по технике
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GymDarkBackground),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📖 Биомеханика и техника выполнения",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )

                    // Исходное положение
                    OutlinedTextField(
                        value = initialPosition,
                        onValueChange = { initialPosition = it },
                        label = { Text("📍 Исходное положение") },
                        placeholder = { Text("Положение стоп, лопаток, хват...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GymTextPrimary,
                            unfocusedTextColor = GymTextPrimary,
                            focusedBorderColor = GymAccentBlue,
                            unfocusedBorderColor = GymDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Выполнение
                    OutlinedTextField(
                        value = execution,
                        onValueChange = { execution = it },
                        label = { Text("🔄 Выполнение движения") },
                        placeholder = { Text("Траектория, темп, фаза дыхания...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GymTextPrimary,
                            unfocusedTextColor = GymTextPrimary,
                            focusedBorderColor = GymAccentBlue,
                            unfocusedBorderColor = GymDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Частые ошибки
                    OutlinedTextField(
                        value = commonMistakes,
                        onValueChange = { commonMistakes = it },
                        label = { Text("⚠️ Частые ошибки") },
                        placeholder = { Text("Отрыв таза, переразгибание коленей, читинг...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GymTextPrimary,
                            unfocusedTextColor = GymTextPrimary,
                            focusedBorderColor = GymAccentBlue,
                            unfocusedBorderColor = GymDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Кнопка сохранения
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreateExercise(
                            name,
                            selectedMuscleGroup,
                            selectedEquipment,
                            restSeconds,
                            initialPosition.ifBlank { null },
                            execution.ifBlank { null },
                            commonMistakes.ifBlank { null }
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymAccentGreen,
                    disabledContainerColor = GymDarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = GymDarkBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Сохранить упражнение",
                    color = if (name.isNotBlank()) GymDarkBackground else GymTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
