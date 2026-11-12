package com.gym.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gym.core.model.Equipment
import com.gym.core.model.MuscleGroup
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentPurple
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurfaceVariant

/**
 * Информативная миниатюра упражнения:
 * 1. Отображает реальную визуальную иллюстрацию движения (GymVisual / Coil Cache).
 * 2. При отсутствии сети или загрузке показывает дифференцированную иконку группы мышц и оборудования,
 *    исключая одинаковые пиктограммы для разных упражнений.
 */
@Composable
fun ExerciseThumbnail(
    thumbnailUrl: String?,
    gifUrl: String?,
    primaryMuscleGroup: MuscleGroup,
    equipment: Equipment,
    modifier: Modifier = Modifier.size(52.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    contentDescription: String? = null
) {
    val muscleEmoji = when (primaryMuscleGroup) {
        MuscleGroup.CHEST -> "🥋"
        MuscleGroup.BACK -> "🦅"
        MuscleGroup.LEGS_QUADS, MuscleGroup.LEGS_HAMSTRINGS, MuscleGroup.CALVES -> "🦵"
        MuscleGroup.SHOULDERS -> "🎯"
        MuscleGroup.BICEPS -> "💪"
        MuscleGroup.TRICEPS -> "⚡"
        MuscleGroup.CORE -> "🛡️"
        MuscleGroup.FULL_BODY -> "🌐"
    }

    val iconBgGradient = when (equipment) {
        Equipment.BARBELL -> Brush.linearGradient(listOf(GymAccentGreen.copy(alpha = 0.25f), GymAccentBlue.copy(alpha = 0.15f)))
        Equipment.DUMBBELL -> Brush.linearGradient(listOf(GymAccentBlue.copy(alpha = 0.25f), GymAccentPurple.copy(alpha = 0.15f)))
        Equipment.CABLE -> Brush.linearGradient(listOf(GymAccentPurple.copy(alpha = 0.25f), GymDarkSurfaceVariant))
        Equipment.MACHINE -> Brush.linearGradient(listOf(Color(0xFFEAB308).copy(alpha = 0.25f), GymDarkSurfaceVariant))
        Equipment.BODYWEIGHT -> Brush.linearGradient(listOf(Color(0xFF06B6D4).copy(alpha = 0.25f), GymDarkSurfaceVariant))
        else -> Brush.linearGradient(listOf(GymDarkSurfaceVariant, GymDarkBackground))
    }

    val imageModel = thumbnailUrl?.takeIf { it.isNotBlank() } ?: gifUrl?.takeIf { it.isNotBlank() }

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (imageModel != null) Color.White else GymDarkSurfaceVariant)
            .border(1.dp, GymDarkSurfaceVariant, shape),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageModel)
                    .crossfade(200)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(iconBgGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = GymAccentGreen.copy(alpha = 0.7f)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(iconBgGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = muscleEmoji, fontSize = 22.sp)
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(iconBgGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(text = muscleEmoji, fontSize = 22.sp)
            }
        }
    }
}
