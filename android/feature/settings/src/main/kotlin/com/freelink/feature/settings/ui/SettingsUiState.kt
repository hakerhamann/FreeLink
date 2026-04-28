package com.freelink.feature.settings.ui

import com.freelink.feature.settings.ui.model.PrivacySettingsUiModel

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val settings: PrivacySettingsUiModel = PrivacySettingsUiModel(),
    val errorMessage: String? = null
)
