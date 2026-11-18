package com.gym.feature.templates.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.UserRole
import com.gym.core.ui.component.GymBrandLogo
import com.gym.core.ui.component.GymBrandGraphicBanner
import com.gym.core.ui.icon.GymIcons
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentGreenDim
import com.gym.core.ui.theme.GymAccentGreenVibrant
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymSurfaceBorder
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Графический Hero-баннер бренда
            GymBrandGraphicBanner(
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Переключатель "Вход" / "Регистрация"
            TabRow(
                selectedTabIndex = if (state.isLoginMode) 0 else 1,
                containerColor = GymDarkSurface,
                contentColor = GymAccentGreen,
                indicator = { tabPositions ->
                    val index = if (state.isLoginMode) 0 else 1
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = GymAccentGreen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, GymSurfaceBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = state.isLoginMode,
                    onClick = { if (!state.isLoginMode) viewModel.handleIntent(AuthIntent.ToggleMode) },
                    text = {
                        Text(
                            text = "Вход",
                            fontWeight = if (state.isLoginMode) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
                Tab(
                    selected = !state.isLoginMode,
                    onClick = { if (state.isLoginMode) viewModel.handleIntent(AuthIntent.ToggleMode) },
                    text = {
                        Text(
                            text = "Регистрация",
                            fontWeight = if (!state.isLoginMode) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Выбор роли (при регистрации)
            AnimatedVisibility(
                visible = !state.isLoginMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ВЫБЕРИТЕ ВАШУ РОЛЬ:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentGreen,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoleSelectionCard(
                            title = "Атлет",
                            subtitle = "Спортсмен",
                            description = "Тренировки, прогрессия 1RM, калькулятор блинов",
                            iconRes = GymIcons.NavWorkout,
                            isSelected = state.selectedRole == UserRole.ATHLETE,
                            onClick = { viewModel.handleIntent(AuthIntent.SelectRole(UserRole.ATHLETE)) },
                            modifier = Modifier.weight(1f)
                        )

                        RoleSelectionCard(
                            title = "Тренер",
                            subtitle = "Наставник",
                            description = "Ведение подопечных, абонементы, видеорецензии",
                            iconRes = GymIcons.NavCoach,
                            isSelected = state.selectedRole == UserRole.COACH,
                            onClick = { viewModel.handleIntent(AuthIntent.SelectRole(UserRole.COACH)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Поле Email
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.handleIntent(AuthIntent.SetEmail(it)) },
                label = { Text("Электронная почта") },
                placeholder = { Text("user@example.com", color = GymTextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = GymAccentGreen
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GymAccentGreen,
                    unfocusedBorderColor = GymSurfaceBorder,
                    focusedLabelColor = GymAccentGreen,
                    unfocusedLabelColor = GymTextSecondary,
                    focusedTextColor = GymTextPrimary,
                    unfocusedTextColor = GymTextPrimary,
                    cursorColor = GymAccentGreen,
                    focusedContainerColor = GymDarkSurface,
                    unfocusedContainerColor = GymDarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Поле Пароль
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.handleIntent(AuthIntent.SetPassword(it)) },
                label = { Text("Пароль") },
                placeholder = { Text("Минимум 6 символов", color = GymTextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = GymAccentGreen
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                            tint = GymTextSecondary
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GymAccentGreen,
                    unfocusedBorderColor = GymSurfaceBorder,
                    focusedLabelColor = GymAccentGreen,
                    unfocusedLabelColor = GymTextSecondary,
                    focusedTextColor = GymTextPrimary,
                    unfocusedTextColor = GymTextPrimary,
                    cursorColor = GymAccentGreen,
                    focusedContainerColor = GymDarkSurface,
                    unfocusedContainerColor = GymDarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Сообщение об ошибке
            if (!state.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GymAccentRed.copy(alpha = 0.15f))
                        .border(1.dp, GymAccentRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        fontSize = 12.sp,
                        color = GymAccentRed,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Главная кнопка Вход / Регистрация
            Button(
                onClick = { viewModel.handleIntent(AuthIntent.Submit) },
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymAccentGreen,
                    contentColor = GymDarkBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = GymDarkBackground,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = if (state.isLoginMode) "Войти" else "Зарегистрироваться",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка гостевого режима (Gym-First / Offline)
            TextButton(
                onClick = { viewModel.handleIntent(AuthIntent.ContinueAsGuest) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Продолжить без аккаунта (Офлайн-режим)",
                    fontSize = 13.sp,
                    color = GymTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    subtitle: String,
    description: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) GymAccentGreenDim else GymDarkSurface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) GymAccentGreen else GymSurfaceBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (isSelected) GymAccentGreenVibrant else GymTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GymAccentGreen)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ВЫБРАНО",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymDarkBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) GymAccentGreenVibrant else GymTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = GymTextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = GymTextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}
