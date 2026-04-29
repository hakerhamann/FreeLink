package com.freelink.feature.settings.ui

internal fun SettingsUiState.privacyLevelLabel(): String {
    val enabledCount = listOf(
        settings.hiddenModeEnabled,
        settings.biometricLockRequired,
        settings.disappearingMessagesEnabled
    ).count { it }

    return when {
        enabledCount >= 2 -> "High"
        enabledCount == 1 -> "Balanced"
        else -> "Standard"
    }
}

internal fun SettingsUiState.profileTitle(): String {
    val rawUserId = userId?.trim().orEmpty()
    if (rawUserId.isBlank()) {
        return "Protected profile"
    }

    return "User ${rawUserId.takeLast(8)}"
}

internal fun SettingsUiState.profileSubtitle(): String {
    val rawDeviceId = deviceId?.trim().orEmpty()
    if (rawDeviceId.isBlank()) {
        return "Secure session details will appear after sign in."
    }

    return "Current device ${rawDeviceId.takeLast(6)} is protected by private defaults."
}
