package com.gym.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.PerceivedEffort
import com.gym.core.ui.theme.GymAccentAmber
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

@Composable
fun PerceivedEffortSelector(
    selectedEffort: PerceivedEffort?,
    onEffortSelect: (PerceivedEffort) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GymDarkSurfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Text(
            text = "Сложность подхода (для автоподбора веса):",
            fontSize = 11.sp,
            color = GymTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PerceivedEffort.values().forEach { effort ->
                val isSelected = selectedEffort == effort
                val (btnColor, activeBorder) = when (effort) {
                    PerceivedEffort.EASY -> Pair(GymAccentGreen, GymAccentGreen)
                    PerceivedEffort.OPTIMAL -> Pair(Color(0xFFEAB308), Color(0xFFEAB308)) // Yellow
                    PerceivedEffort.HARD -> Pair(GymAccentAmber, GymAccentAmber)          // Amber/Orange
                    PerceivedEffort.FAILURE -> Pair(GymAccentRed, GymAccentRed)           // Red
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) btnColor.copy(alpha = 0.25f) else GymDarkSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) activeBorder else GymDarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onEffortSelect(effort) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = effort.shortLabel,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) btnColor else GymTextPrimary
                        )
                        Text(
                            text = effort.approximateRir,
                            fontSize = 10.sp,
                            color = if (isSelected) btnColor.copy(alpha = 0.8f) else GymTextSecondary
                        )
                    }
                }
            }
        }
    }
}
