package com.freelink.feature.profile.ui

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

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, errorMessage = null) }
            when (val result = authRepository.logout()) {
                is AuthRepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            isLoggedOut = true
                        )
                    }
                }
                is AuthRepositoryResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionFlow.collect { session ->
                _uiState.update {
                    it.copy(
                        userId = session?.userId,
                        deviceId = session?.deviceId,
                        isSessionAvailable = session != null,
                        isLoggedOut = session == null && it.isLoggedOut
                    )
                }
            }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ProfileViewModel(authRepository)
                }
            }
        }
    }
}
