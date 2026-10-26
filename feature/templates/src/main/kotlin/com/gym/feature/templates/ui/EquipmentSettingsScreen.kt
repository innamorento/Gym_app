package com.gym.feature.templates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.Equipment
import com.gym.core.model.EquipmentWeightProfile
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSettingsScreen(
    onBackClick: () -> Unit,
    onSaveClick: (EquipmentWeightProfile) -> Unit = {}
) {
    // Настройки гантелей
    var dumbbellSmallStep by remember { mutableDoubleStateOf(1.0) }
    var dumbbellThreshold by remember { mutableDoubleStateOf(10.0) }
    var dumbbellHeavyStep by remember { mutableDoubleStateOf(2.0) }

    // Настройки блоков
    var cableStep by remember { mutableDoubleStateOf(2.5) }

    // Настройки штанги
    var barWeight by remember { mutableDoubleStateOf(20.0) }
    var minPlatesPair by remember { mutableDoubleStateOf(2.5) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Шаг весов и тренажеры",
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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить",
                            tint = GymAccentGreen
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
                Text(
                    text = "Настройте реальную дискретность снарядов вашего зала для точной автопрогрессии весов.",
                    fontSize = 14.sp,
                    color = GymTextSecondary
                )
            }

            // Гантели
            item {
                EquipmentConfigCard(
                    title = "Гантельный ряд",
                    subtitle = "Градация шага веса легких и тяжелых гантелей"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Порог легких гантелей: ${dumbbellThreshold.toInt()} кг", color = GymTextPrimary)
                        }
                        Slider(
                            value = dumbbellThreshold.toFloat(),
                            onValueChange = { dumbbellThreshold = it.toDouble() },
                            valueRange = 6f..14f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = GymAccentGreen,
                                activeTrackColor = GymAccentGreen
                            )
                        )

                        Text("Шаг до ${dumbbellThreshold.toInt()} кг: $dumbbellSmallStep кг (1, 2, 3...)", color = GymTextSecondary, fontSize = 13.sp)
                        Text("Шаг свыше ${dumbbellThreshold.toInt()} кг: $dumbbellHeavyStep кг (12, 14, 16...)", color = GymTextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // Блочные тренажеры и кроссоверы
            item {
                EquipmentConfigCard(
                    title = "Блочные тренажеры (Cables / Stacks)",
                    subtitle = "Шаг пластин в грузоблочных тренажерах"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Текущий шаг грузоблока: $cableStep кг", color = GymTextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StepChip(label = "2.5 кг", isSelected = cableStep == 2.5) { cableStep = 2.5 }
                            StepChip(label = "5.0 кг", isSelected = cableStep == 5.0) { cableStep = 5.0 }
                            StepChip(label = "1.25 кг (с микроплиткой)", isSelected = cableStep == 1.25) { cableStep = 1.25 }
                        }
                    }
                }
            }

            // Штанги и блины
            item {
                EquipmentConfigCard(
                    title = "Штанги и диски (Barbell)",
                    subtitle = "Базовый вес грифа и минимальный шаг блинов"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Вес грифа: ${barWeight.toInt()} кг", color = GymTextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StepChip(label = "20 кг (Мужской)", isSelected = barWeight == 20.0) { barWeight = 20.0 }
                            StepChip(label = "15 кг (Женский)", isSelected = barWeight == 15.0) { barWeight = 15.0 }
                            StepChip(label = "10 кг (EZ-гриф)", isSelected = barWeight == 10.0) { barWeight = 10.0 }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Минимальный шаг блинов на сторону: ${minPlatesPair / 2.0} кг (шаг штанги $minPlatesPair кг)", color = GymTextSecondary, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StepChip(label = "2 x 1.25 кг (шаг 2.5)", isSelected = minPlatesPair == 2.5) { minPlatesPair = 2.5 }
                            StepChip(label = "2 x 0.5 кг (микродиски, шаг 1.0)", isSelected = minPlatesPair == 1.0) { minPlatesPair = 1.0 }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentConfigCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = GymAccentBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GymTextPrimary)
            }
            Text(text = subtitle, fontSize = 12.sp, color = GymTextSecondary)
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun StepChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = GymAccentGreen,
            selectedLabelColor = GymDarkBackground,
            containerColor = GymDarkSurfaceVariant,
            labelColor = GymTextPrimary
        )
    )
}
