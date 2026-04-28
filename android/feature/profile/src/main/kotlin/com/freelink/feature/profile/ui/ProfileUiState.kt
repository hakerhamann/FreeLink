package com.freelink.feature.profile.ui

data class ProfileUiState(
    val userId: String? = null,
    val deviceId: String? = null,
    val isSessionAvailable: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null
)
