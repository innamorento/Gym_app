package com.gym.feature.templates.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.database.entity.UserProfileEntity
import com.gym.core.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class AuthUiState(
    val isLoginMode: Boolean = false,
    val email: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.ATHLETE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isGuestMode: Boolean = false
)

sealed interface AuthIntent {
    data object ToggleMode : AuthIntent
    data class SetEmail(val email: String) : AuthIntent
    data class SetPassword(val password: String) : AuthIntent
    data class SelectRole(val role: UserRole) : AuthIntent
    data object Submit : AuthIntent
    data object ContinueAsGuest : AuthIntent
}

class AuthViewModel(
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.ToggleMode -> {
                _uiState.update { it.copy(isLoginMode = !it.isLoginMode, errorMessage = null) }
            }
            is AuthIntent.SetEmail -> {
                _uiState.update { it.copy(email = intent.email, errorMessage = null) }
            }
            is AuthIntent.SetPassword -> {
                _uiState.update { it.copy(password = intent.password, errorMessage = null) }
            }
            is AuthIntent.SelectRole -> {
                _uiState.update { it.copy(selectedRole = intent.role, errorMessage = null) }
            }
            AuthIntent.Submit -> submit()
            AuthIntent.ContinueAsGuest -> continueAsGuest()
        }
    }

    private fun submit() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password.trim()

        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
            _uiState.update { it.copy(errorMessage = "Введите корректный адрес электронной почты") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Пароль должен содержать не менее 6 символов") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val existing = userProfileDao.getUserProfileSync()

            val coachCode = if (state.selectedRole == UserRole.COACH) {
                existing?.coachInviteCode ?: "APEX-COACH-${Random.nextInt(1000, 9999)}"
            } else {
                null
            }

            val updatedProfile = (existing ?: UserProfileEntity(id = "default_user")).copy(
                email = email,
                role = if (state.isLoginMode) (existing?.role ?: state.selectedRole) else state.selectedRole,
                isGuest = false,
                coachInviteCode = coachCode
            )

            userProfileDao.saveUserProfile(updatedProfile)

            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    private fun continueAsGuest() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val existing = userProfileDao.getUserProfileSync()

            val guestProfile = (existing ?: UserProfileEntity(id = "default_user")).copy(
                email = null,
                role = UserRole.ATHLETE,
                isGuest = true
            )

            userProfileDao.saveUserProfile(guestProfile)
            _uiState.update { it.copy(isLoading = false, isSuccess = true, isGuestMode = true) }
        }
    }
}
