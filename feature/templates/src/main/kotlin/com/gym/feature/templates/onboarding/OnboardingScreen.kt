package com.gym.feature.templates.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.FitnessGoal
import com.gym.core.model.Gender
import com.gym.core.model.TrainingExperience
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onOnboardingComplete()
        }
    }

    Scaffold(
        containerColor = GymDarkBackground,
        bottomBar = {
            OnboardingBottomBar(
                currentStep = state.currentStep,
                totalSteps = TOTAL_STEPS,
                canProceed = state.currentStep != 0 || state.name.isNotBlank(),
                isSaving = state.isSaving,
                onPreviousClick = { viewModel.handleIntent(OnboardingIntent.PreviousStep) },
                onNextClick = { viewModel.handleIntent(OnboardingIntent.NextStep) },
                onCompleteClick = { viewModel.handleIntent(OnboardingIntent.CompleteOnboarding) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step Indicator Dots
            StepIndicator(
                currentStep = state.currentStep,
                totalSteps = TOTAL_STEPS
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_step_transition",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    0 -> PersonalInfoStep(
                        name = state.name,
                        gender = state.gender,
                        age = state.age,
                        onNameChange = { viewModel.handleIntent(OnboardingIntent.SetName(it)) },
                        onGenderChange = { viewModel.handleIntent(OnboardingIntent.SetGender(it)) },
                        onAgeChange = { viewModel.handleIntent(OnboardingIntent.SetAge(it)) }
                    )
                    1 -> AnthropometryStep(
                        heightCm = state.heightCm,
                        weightKg = state.weightKg,
                        bmi = state.bmi,
                        onHeightChange = { viewModel.handleIntent(OnboardingIntent.SetHeight(it)) },
                        onWeightChange = { viewModel.handleIntent(OnboardingIntent.SetWeight(it)) }
                    )
                    2 -> TrainingProfileStep(
                        experience = state.experience,
                        goal = state.primaryGoal,
                        workoutsPerWeek = state.workoutsPerWeek,
                        recommendedProgram = state.recommendedProgram,
                        onExperienceChange = { viewModel.handleIntent(OnboardingIntent.SetExperience(it)) },
                        onGoalChange = { viewModel.handleIntent(OnboardingIntent.SetGoal(it)) },
                        onWorkoutsChange = { viewModel.handleIntent(OnboardingIntent.SetWorkoutsPerWeek(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isDone = index < currentStep
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .width(if (isActive) 28.dp else 10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            isActive -> GymAccentGreen
                            isDone -> GymAccentGreen.copy(alpha = 0.5f)
                            else -> GymDarkSurfaceVariant
                        }
                    )
            )
        }
    }
}

// ── Step 0: Персональные данные ───────────────────────────────────

@Composable
private fun PersonalInfoStep(
    name: String,
    gender: Gender,
    age: Int,
    onNameChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onAgeChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "👋 Добро пожаловать!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GymTextPrimary
        )

        Text(
            text = "Расскажите немного о себе, чтобы тренировочные программы и алгоритм автопрогрессии были максимально точными.",
            fontSize = 15.sp,
            color = GymTextSecondary,
            lineHeight = 22.sp
        )

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val focusRequester = remember { FocusRequester() }

        // Имя
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Ваше имя или никнейм",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GymTextPrimary
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("Введите имя", color = GymTextSecondary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                trailingIcon = {
                    if (name.isNotBlank()) {
                        IconButton(onClick = { onNameChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Очистить",
                                tint = GymTextSecondary
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GymTextPrimary,
                    unfocusedTextColor = GymTextPrimary,
                    focusedBorderColor = GymAccentGreen,
                    unfocusedBorderColor = GymDarkSurfaceVariant,
                    focusedContainerColor = GymDarkSurface,
                    unfocusedContainerColor = GymDarkSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Пол
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Пол",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GymTextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Gender.entries.forEach { item ->
                    val isSelected = item == gender
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onGenderChange(item) }
                            .then(
                                if (isSelected) Modifier.border(2.dp, GymAccentGreen, RoundedCornerShape(12.dp))
                                else Modifier
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GymDarkSurfaceVariant else GymDarkSurface
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (item == Gender.MALE) "♂ Мужской" else "♀ Женский",
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) GymAccentGreen else GymTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Возраст
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Возраст (лет)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GymTextPrimary
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (age > 14) onAgeChange(age - 1) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Меньше", tint = GymTextPrimary)
                    }

                    Text(
                        text = "$age",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentGreen
                    )

                    IconButton(
                        onClick = { if (age < 90) onAgeChange(age + 1) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GymDarkSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Больше", tint = GymTextPrimary)
                    }
                }
            }
        }
    }
}

// ── Step 1: Антропометрия ──────────────────────────────────────────

@Composable
private fun AnthropometryStep(
    heightCm: Float,
    weightKg: Float,
    bmi: Float,
    onHeightChange: (Float) -> Unit,
    onWeightChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "📏 Параметры тела",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GymTextPrimary
        )

        Text(
            text = "Рост и вес используются для расчёта индекса массы тела (ИМТ) и корректной прогрессии нагрузок.",
            fontSize = 15.sp,
            color = GymTextSecondary,
            lineHeight = 22.sp
        )

        // Рост
        Card(
            colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Рост", color = GymTextSecondary, fontSize = 14.sp)
                    Text(
                        text = "${heightCm.toInt()} см",
                        color = GymAccentGreen,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = heightCm,
                    onValueChange = { onHeightChange(it) },
                    valueRange = 140f..220f,
                    colors = SliderDefaults.colors(
                        thumbColor = GymAccentGreen,
                        activeTrackColor = GymAccentGreen,
                        inactiveTrackColor = GymDarkSurfaceVariant
                    )
                )
            }
        }

        // Вес
        Card(
            colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Текущий вес", color = GymTextSecondary, fontSize = 14.sp)
                    Text(
                        text = "${String.format("%.1f", weightKg)} кг",
                        color = GymAccentGreen,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = weightKg,
                    onValueChange = { onWeightChange(it) },
                    valueRange = 40f..180f,
                    steps = 279, // шаг 0.5 кг
                    colors = SliderDefaults.colors(
                        thumbColor = GymAccentGreen,
                        activeTrackColor = GymAccentGreen,
                        inactiveTrackColor = GymDarkSurfaceVariant
                    )
                )
            }
        }

        // BMI Card
        val (bmiCategory, bmiColor) = when {
            bmi < 18.5f -> "Недостаточный вес" to GymAccentBlue
            bmi < 25.0f -> "Нормальный вес" to GymAccentGreen
            bmi < 30.0f -> "Избыточный вес" to Color(0xFFF59E0B)
            else -> "Ожирение" to Color(0xFFEF4444)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, bmiColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Индекс массы тела (ИМТ)", fontSize = 13.sp, color = GymTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bmiCategory,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = bmiColor
                    )
                }
                Text(
                    text = String.format("%.1f", bmi),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = bmiColor
                )
            }
        }
    }
}

// ── Step 2: Тренировочный профиль ──────────────────────────────────

@Composable
private fun TrainingProfileStep(
    experience: TrainingExperience,
    goal: FitnessGoal,
    workoutsPerWeek: Int,
    recommendedProgram: String,
    onExperienceChange: (TrainingExperience) -> Unit,
    onGoalChange: (FitnessGoal) -> Unit,
    onWorkoutsChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "🎯 Ваши цели",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GymTextPrimary
        )

        // Стаж
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Тренировочный опыт", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GymTextPrimary)
            TrainingExperience.entries.forEach { exp ->
                val isSelected = exp == experience
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onExperienceChange(exp) }
                        .then(
                            if (isSelected) Modifier.border(2.dp, GymAccentGreen, RoundedCornerShape(12.dp))
                            else Modifier
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GymDarkSurfaceVariant else GymDarkSurface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = exp.displayNameRu,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) GymAccentGreen else GymTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = exp.subtitleRu,
                            fontSize = 12.sp,
                            color = GymTextSecondary
                        )
                    }
                }
            }
        }

        // Главная цель
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Главная цель", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GymTextPrimary)
            FitnessGoal.entries.forEach { g ->
                val isSelected = g == goal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGoalChange(g) }
                        .then(
                            if (isSelected) Modifier.border(2.dp, GymAccentGreen, RoundedCornerShape(12.dp))
                            else Modifier
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GymDarkSurfaceVariant else GymDarkSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = g.emoji, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = g.displayNameRu,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GymAccentGreen else GymTextPrimary
                            )
                            Text(
                                text = g.descriptionRu,
                                fontSize = 12.sp,
                                color = GymTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Частота тренировок в неделю
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Тренировок в неделю", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GymTextPrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2, 3, 4, 5).forEach { count ->
                    val isSelected = count == workoutsPerWeek
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onWorkoutsChange(count) }
                            .then(
                                if (isSelected) Modifier.border(2.dp, GymAccentGreen, RoundedCornerShape(10.dp))
                                else Modifier
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GymDarkSurfaceVariant else GymDarkSurface
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "$count дня",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) GymAccentGreen else GymTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Рекомендованная программа
        if (recommendedProgram.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GymDarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💡", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "Рекомендованная программа:",
                            fontSize = 12.sp,
                            color = GymTextSecondary
                        )
                        Text(
                            text = recommendedProgram,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Нижняя панель действий ──────────────────────────────────────────

@Composable
private fun OnboardingBottomBar(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    isSaving: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GymDarkBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onPreviousClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GymTextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Назад", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (currentStep < totalSteps - 1) {
            Button(
                onClick = onNextClick,
                enabled = canProceed,
                modifier = Modifier
                    .weight(if (currentStep > 0) 1.5f else 1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymAccentGreen,
                    contentColor = GymDarkBackground,
                    disabledContainerColor = GymDarkSurfaceVariant,
                    disabledContentColor = GymTextSecondary
                )
            ) {
                Text("Далее", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Далее",
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Button(
                onClick = onCompleteClick,
                enabled = !isSaving,
                modifier = Modifier
                    .weight(if (currentStep > 0) 2f else 1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymAccentGreen,
                    contentColor = GymDarkBackground
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = GymDarkBackground,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Начать тренировки 🚀", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
