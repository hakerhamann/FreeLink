package com.freelink.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.settings.data.PrivacySettingsRepository
import com.freelink.feature.settings.data.PrivacySettingsResult
import com.freelink.feature.settings.ui.mapper.toDomain
import com.freelink.feature.settings.ui.mapper.toUiModel
import com.freelink.feature.settings.ui.model.PrivacySettingsUiModel
import com.freelink.feature.settings.ui.model.WhoCanMessageUiOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: PrivacySettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        observeLocalSettings()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isLoggedOut = false) }
            when (val result = repository.refreshSettings()) {
                is PrivacySettingsResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                }
                is PrivacySettingsResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onHiddenModeChanged(enabled: Boolean) {
        updateSettings { it.copy(hiddenModeEnabled = enabled) }
    }

    fun onBiometricLockChanged(enabled: Boolean) {
        updateSettings { it.copy(biometricLockRequired = enabled) }
    }

    fun onDisappearingMessagesChanged(enabled: Boolean) {
        updateSettings { it.copy(disappearingMessagesEnabled = enabled) }
    }

    fun onLinkPreviewChanged(enabled: Boolean) {
        updateSettings { it.copy(linkPreviewEnabled = enabled) }
    }

    fun onWhoCanMessageChanged(option: WhoCanMessageUiOption) {
        updateSettings { it.copy(whoCanMessageMe = option) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, errorMessage = null) }
            when (val result = repository.logout()) {
                is PrivacySettingsResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            isLoggedOut = true
                        )
                    }
                }
                is PrivacySettingsResult.Failure -> {
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

    private fun observeLocalSettings() {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        settings = settings.toUiModel(),
                        isSaving = false
                    )
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
                        isSessionAvailable = session != null
                    )
                }
            }
        }
    }

    private fun updateSettings(transform: (PrivacySettingsUiModel) -> PrivacySettingsUiModel) {
        val previousSettings = _uiState.value.settings
        val nextSettings = transform(previousSettings)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    settings = nextSettings,
                    isSaving = true,
                    errorMessage = null
                )
            }

            when (val result = repository.updateSettings(nextSettings.toDomain())) {
                is PrivacySettingsResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                }
                is PrivacySettingsResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            settings = previousSettings,
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(
            repository: PrivacySettingsRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SettingsViewModel(repository, authRepository)
                }
            }
        }
    }
}
