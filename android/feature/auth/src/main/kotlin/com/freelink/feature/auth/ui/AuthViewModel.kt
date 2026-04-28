package com.freelink.feature.auth.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onLoginChanged(value: String) {
        _uiState.update { it.copy(login = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun submitLogin() {
        val current = _uiState.value
        val login = current.login.trim()
        val password = current.password

        if (login.length < 3 || password.length < 8) {
            _uiState.update {
                it.copy(errorMessage = "Логин от 3 символов, пароль от 8 символов.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                isAuthorized = true,
                errorMessage = null
            )
        }
    }
}

