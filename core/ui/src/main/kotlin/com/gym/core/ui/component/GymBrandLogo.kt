package com.gym.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.ui.R
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentGreenVibrant
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

/**
 * Фирменный логотип «The Iron Apex» и визуальный стиль бренда Gym Workout Tracker.
 */
@Composable
fun GymBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showWordmark: Boolean = false,
    subtitle: String? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.25f))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GymDarkSurfaceVariant,
                            GymDarkBackground
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            GymAccentGreen.copy(alpha = 0.6f),
                            GymAccentBlue.copy(alpha = 0.3f)
                        )
                    ),
                    RoundedCornerShape(size * 0.25f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_gym_logo),
                contentDescription = "The Iron Apex Logo",
                modifier = Modifier.size(size * 0.95f)
            )
        }

        if (showWordmark) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "IRON",
                        fontSize = (size.value * 0.42f).sp,
                        fontWeight = FontWeight.Black,
                        color = GymTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "APEX",
                        fontSize = (size.value * 0.42f).sp,
                        fontWeight = FontWeight.Black,
                        color = GymAccentGreen,
                        letterSpacing = 1.sp
                    )
                }
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GymTextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Крупный приветственный баннер бренда с фирменным стилем Tactical Cyber Lime.
 */
@Composable
fun GymBrandHeroBanner(
    modifier: Modifier = Modifier,
    title: String = "THE IRON APEX",
    subtitle: String = "Умный трекинг тренировок и прогрессии"
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        GymDarkSurface,
                        GymDarkSurfaceVariant
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        GymAccentGreen.copy(alpha = 0.5f),
                        GymAccentBlue.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            GymBrandLogo(size = 54.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = GymTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GymAccentGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreenVibrant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = GymTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
