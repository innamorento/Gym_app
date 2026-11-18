package com.gym.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.ui.icon.GymIcons
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymSurfaceBorder
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import com.gym.core.ui.theme.GymTheme

/**
 * Интерактивная витрина дизайн-системы Tactical Iron Hex & Cyber Lime.
 * Позволяет визуально валидировать все иконки, цвета и логотипы в едином контексте.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GymIconsShowcase(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(GymDarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок бренда
        GymBrandHeroBanner(
            title = "THE IRON APEX",
            subtitle = "Фирменная дизайн-система Tactical Iron Hex"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Нижняя панель навигации
        ShowcaseSection(title = "НИЖНЯЯ НАВИГАЦИЯ (BOTTOM NAV)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShowcaseIconCard("Тренировка", GymIcons.NavWorkout)
                ShowcaseIconCard("История", GymIcons.NavHistory)
                ShowcaseIconCard("Каталог", GymIcons.NavCatalog)
                ShowcaseIconCard("Аналитика", GymIcons.NavAnalytics)
                ShowcaseIconCard("Тренер", GymIcons.NavCoach)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Ключевые действия тренировки
        ShowcaseSection(title = "ДЕЙСТВИЯ И УТИЛИТЫ (WORKOUT ACTIONS)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShowcaseIconCard("Таймер", GymIcons.Timer)
                ShowcaseIconCard("Сет готов", GymIcons.CheckHex)
                ShowcaseIconCard("Замена", GymIcons.Swap)
                ShowcaseIconCard("Блины", GymIcons.PlateCalc)
                ShowcaseIconCard("RPE 10", GymIcons.Bolt)
                ShowcaseIconCard("Видео", GymIcons.Videocam)
                ShowcaseIconCard("Рекорд PR", GymIcons.TrophyHex)
                ShowcaseIconCard("Суперсет", GymIcons.SupersetLink)
                ShowcaseIconCard("Калибровка", GymIcons.EquipmentTune)
                ShowcaseIconCard("Бэкап БД", GymIcons.DatabaseBackup)
                ShowcaseIconCard("Поделиться", GymIcons.ShareRecord)
                ShowcaseIconCard("Логотип", GymIcons.GymLogo)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Анатомический атлас мышечных групп
        ShowcaseSection(title = "АНАТОМИЧЕСКИЙ АТЛАС (MUSCLE GROUPS)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShowcaseIconCard("Грудь", GymIcons.MuscleChest)
                ShowcaseIconCard("Спина", GymIcons.MuscleBack)
                ShowcaseIconCard("Ноги", GymIcons.MuscleLegs)
                ShowcaseIconCard("Плечи", GymIcons.MuscleShoulders)
                ShowcaseIconCard("Бицепс", GymIcons.MuscleBiceps)
                ShowcaseIconCard("Трицепс", GymIcons.MuscleTriceps)
                ShowcaseIconCard("Пресс", GymIcons.MuscleCore)
                ShowcaseIconCard("Все тело", GymIcons.MuscleOther)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Главные тренировочные цели
        ShowcaseSection(title = "ФИТНЕС-ЦЕЛИ (FITNESS GOALS)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShowcaseIconCard("Масса", GymIcons.GoalHypertrophy)
                ShowcaseIconCard("Сила", GymIcons.GoalStrength)
                ShowcaseIconCard("Рельеф", GymIcons.GoalFatLoss)
                ShowcaseIconCard("Здоровье", GymIcons.GoalHealth)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Комбинированные сплиты тренировок
        ShowcaseSection(title = "СПЛИТЫ ТРЕНИРОВОК (WORKOUT SPLITS)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShowcaseIconCard("Спина+Бицепс", GymIcons.SplitBackBiceps)
                ShowcaseIconCard("Грудь+Трицепс", GymIcons.SplitChestTriceps)
                ShowcaseIconCard("Ноги+Плечи", GymIcons.SplitLegsShoulders)
                ShowcaseIconCard("Грудь+Спина", GymIcons.SplitChestBack)
                ShowcaseIconCard("Плечи+Руки", GymIcons.SplitShouldersArms)
                ShowcaseIconCard("Верх тела", GymIcons.SplitUpperBody)
                ShowcaseIconCard("Низ тела", GymIcons.SplitLowerBody)
                ShowcaseIconCard("Фулбоди", GymIcons.SplitFullBody)
            }
        }
    }
}

@Composable
private fun ShowcaseSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GymAccentGreen,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun ShowcaseIconCard(
    label: String,
    iconRes: Int
) {
    Box(
        modifier = Modifier
            .size(width = 76.dp, height = 76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GymDarkSurface)
            .border(1.dp, GymSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = Color.Unspecified, // Сохраняем фирменные цвета вектора
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = GymTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Preview(name = "Tactical Iron Hex - Icons Showcase", showBackground = true, backgroundColor = 0xFF0A0B0E)
@Composable
private fun GymIconsShowcasePreview() {
    GymTheme {
        GymIconsShowcase()
    }
}
