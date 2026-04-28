package com.freelink.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val loginResult = authRepository.login(login, password)) {
                is AuthRepositoryResult.Success -> {
                    val devicesCount = when (val devicesResult = authRepository.loadDevices()) {
                        is AuthRepositoryResult.Success -> devicesResult.value.size
                        is AuthRepositoryResult.Failure -> 0
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthorized = true,
                            knownDevicesCount = devicesCount,
                            errorMessage = null
                        )
                    }
                }
                is AuthRepositoryResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthorized = false,
                            errorMessage = loginResult.message
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    AuthViewModel(authRepository)
                }
            }
        }
    }
}
