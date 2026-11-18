package com.gym.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.gym.core.model.PerceivedEffort
import com.gym.core.ui.theme.GymAccentAmber
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

// [AI-TASK:TASK-022] Compact collapsable PerceivedEffortSelector to prevent gym screen clutter
@Composable
fun PerceivedEffortSelector(
    selectedEffort: PerceivedEffort?,
    onEffortSelect: (PerceivedEffort) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(selectedEffort == null) { mutableStateOf(selectedEffort == null) }

    if (!isExpanded && selectedEffort != null) {
        val (badgeColor, activeBorder) = when (selectedEffort) {
            PerceivedEffort.EASY -> Pair(GymAccentGreen, GymAccentGreen)
            PerceivedEffort.OPTIMAL -> Pair(Color(0xFFEAB308), Color(0xFFEAB308))
            PerceivedEffort.HARD -> Pair(GymAccentAmber, GymAccentAmber)
            PerceivedEffort.FAILURE -> Pair(GymAccentRed, GymAccentRed)
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.12f))
                .border(1.dp, activeBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { isExpanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${selectedEffort.shortLabel} (${selectedEffort.approximateRir})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "изменить",
                    fontSize = 11.sp,
                    color = GymTextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Изменить сложность",
                    tint = GymTextSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    } else {
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
                        PerceivedEffort.OPTIMAL -> Pair(Color(0xFFEAB308), Color(0xFFEAB308))
                        PerceivedEffort.HARD -> Pair(GymAccentAmber, GymAccentAmber)
                        PerceivedEffort.FAILURE -> Pair(GymAccentRed, GymAccentRed)
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
                            .clickable {
                                onEffortSelect(effort)
                                isExpanded = false
                            }
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
}
