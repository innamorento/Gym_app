package com.gym.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Дизайн-система «Tactical Cyber Lime» для Gym Workout Tracker.
 * Оптимизирована для высокой контрастности в условиях яркого освещения спортзала
 * и минимального расхода батареи на OLED/AMOLED дисплеях.
 */

// Поверхности и фоны (Onyx & Charcoal)
val GymDarkBackground = Color(0xFF0A0B0E)       // Глубокий оникс (OLED-friendly)
val GymDarkSurface = Color(0xFF14161D)          // Темный антрацит (карточки, диалоги, шторки)
val GymDarkSurfaceVariant = Color(0xFF1F2430)   // Акцентная поверхность (инпуты, степперы, контейнеры)
val GymSurfaceBorder = Color(0xFF2A3241)        // Тонкие технологичные границы и разделители

// Акцентные цвета
val GymAccentGreen = Color(0xFF22C55E)          // Неоновый Cyber Lime (завершение сетов, прогресс)
val GymAccentGreenVibrant = Color(0xFF4ADE80)   // Светящийся лайм (яркие иконки, статус 1RM)
val GymAccentGreenDim = Color(0x2622C55E)       // 15% прозрачность лайма (подложки чипсов)

val GymAccentBlue = Color(0xFF38BDF8)           // Ледяной синий (таймер отдыха, аналитика)
val GymAccentAmber = Color(0xFFF59E0B)          // Электрический янтарь (разминочные сеты)
val GymAccentPurple = Color(0xFFA855F7)         // Неоновый фиолетовый (суперсеты, дропсеты)
val GymAccentRed = Color(0xFFEF4444)            // Сигнальный красный (отказные сеты, удаление)

// Типографика (High-Contrast Slate)
val GymTextPrimary = Color(0xFFF9FAFB)          // Высококонтрастный белый
val GymTextSecondary = Color(0xFF94A3B8)        // Нейтральный сланец (подписи, единицы измерения)
val GymTextTertiary = Color(0xFF64748B)         // Приглушенный сланец (плейсхолдеры, отключенные элементы)
