package com.gym.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymSurfaceBorder
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

/**
 * Цвета олимпийских блинов по стандартам IWF/IPF
 */
private fun getPlateColor(weight: Double): Color = when (weight) {
    25.0 -> Color(0xFFEF4444)  // Красный
    20.0 -> Color(0xFF3B82F6)  // Синий
    15.0 -> Color(0xFFEAB308)  // Желтый
    10.0 -> Color(0xFF22C55E)  // Зеленый
    5.0 -> Color(0xFFF9FAFB)   // Белый
    2.5 -> Color(0xFF94A3B8)   // Серебристый/Черный
    1.25 -> Color(0xFF38BDF8)  // Хром / Ледяной
    else -> GymAccentBlue
}

/**
 * Калькулятор блинов на гриф штанги (Plate Calculator).
 * Рассчитывает точный набор блинов на каждую сторону грифа.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlateCalculatorDialog(
    initialTargetWeight: Double,
    onDismiss: () -> Unit,
    onApplyWeight: (Double) -> Unit
) {
    var targetWeight by remember { mutableDoubleStateOf(initialTargetWeight.coerceAtLeast(0.0)) }
    var barWeight by remember { mutableDoubleStateOf(20.0) } // Стандартный олимпийский гриф 20 кг
    var weightText by remember {
        mutableStateOf(if (initialTargetWeight > 0) initialTargetWeight.toString() else "20.0")
    }

    // Расчет блинов на одну сторону
    val weightOnSleeves = (targetWeight - barWeight).coerceAtLeast(0.0)
    val weightPerSide = weightOnSleeves / 2.0

    val availablePlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    val calculatedPlates = remember(weightPerSide) {
        val result = mutableListOf<Pair<Double, Int>>()
        var remaining = weightPerSide
        for (plate in availablePlates) {
            val count = (remaining / plate).toInt()
            if (count > 0) {
                result.add(plate to count)
                remaining -= count * plate
            }
        }
        result to remaining
    }

    val plateCounts = calculatedPlates.first
    val remainder = calculatedPlates.second

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GymDarkSurface,
        title = {
            Text(
                text = "🏋️ Калькулятор блинов",
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Целевой вес штанги (кг):",
                    fontSize = 12.sp,
                    color = GymTextSecondary
                )
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
                                targetWeight = (targetWeight - 2.5).coerceAtLeast(barWeight)
                                weightText = if (targetWeight % 1.0 == 0.0) targetWeight.toInt().toString() else targetWeight.toString()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("-2.5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GymTextPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = {
                            weightText = it
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null) targetWeight = parsed
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(130.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontFeatureSettings = "tnum", fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GymTextPrimary,
                            unfocusedTextColor = GymTextPrimary,
                            focusedBorderColor = GymAccentGreen,
                            unfocusedBorderColor = GymSurfaceBorder
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
                                targetWeight = (targetWeight + 2.5).coerceAtLeast(barWeight)
                                weightText = if (targetWeight % 1.0 == 0.0) targetWeight.toInt().toString() else targetWeight.toString()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("+2.5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GymAccentGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Выбор веса самого грифа
                Text(
                    text = "Вес грифа:",
                    fontSize = 12.sp,
                    color = GymTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(20.0 to "20 кг (Олимпийский)", 15.0 to "15 кг (Женский)", 10.0 to "10 кг (EZ-гриф)").forEach { (weight, label) ->
                        val selected = barWeight == weight
                        FilterChip(
                            selected = selected,
                            onClick = {
                                barWeight = weight
                                if (targetWeight < barWeight) {
                                    targetWeight = barWeight
                                    weightText = barWeight.toString()
                                }
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymAccentBlue.copy(alpha = 0.2f),
                                selectedLabelColor = GymAccentBlue,
                                containerColor = GymDarkSurfaceVariant,
                                labelColor = GymTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Результаты расчета блинов на каждую сторону
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GymSurfaceBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = GymDarkBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("На каждую сторону:", fontSize = 12.sp, color = GymTextSecondary)
                            Text(
                                text = "%.2f кг".format(weightPerSide),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymAccentGreen,
                                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (plateCounts.isEmpty()) {
                            Text(
                                text = if (targetWeight == barWeight) "Пустой гриф (без блинов)" else "Целевой вес меньше веса грифа",
                                fontSize = 12.sp,
                                color = GymTextSecondary
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                plateCounts.forEach { (plateWeight, count) ->
                                    val color = getPlateColor(plateWeight)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(color.copy(alpha = 0.15f))
                                            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "${count}x ${if (plateWeight % 1.0 == 0.0) plateWeight.toInt() else plateWeight} кг",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GymTextPrimary,
                                            style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                                        )
                                    }
                                }
                            }
                        }

                        if (remainder > 0.01) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ Не делится кратно 1.25 кг (остаток %.2f кг)".format(remainder * 2),
                                fontSize = 11.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyWeight(targetWeight)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
            ) {
                Text("Применить вес", color = GymDarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = GymTextSecondary)
            }
        }
    )
}
