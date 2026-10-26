package com.gym.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.gym.core.model.SetType
import com.gym.core.ui.theme.GymAccentAmber
import com.gym.core.ui.theme.GymAccentPurple
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary

@Composable
fun SetTypeBadge(
    setNumber: Int,
    setType: SetType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, textColor, label) = when (setType) {
        SetType.WARMUP -> Triple(GymAccentAmber.copy(alpha = 0.2f), GymAccentAmber, "W")
        SetType.DROP -> Triple(GymAccentPurple.copy(alpha = 0.2f), GymAccentPurple, "D")
        SetType.FAILURE -> Triple(GymAccentRed.copy(alpha = 0.2f), GymAccentRed, "F")
        SetType.NORMAL -> Triple(GymDarkSurfaceVariant, GymTextPrimary, setNumber.toString())
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
