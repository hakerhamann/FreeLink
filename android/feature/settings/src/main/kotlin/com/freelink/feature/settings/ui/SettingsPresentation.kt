package com.freelink.feature.settings.ui

internal fun SettingsUiState.privacyLevelLabel(): String {
    val enabledCount = listOf(
        settings.hiddenModeEnabled,
        settings.biometricLockRequired,
        settings.disappearingMessagesEnabled
    ).count { it }

    return when {
        enabledCount >= 2 -> "высокая"
        enabledCount == 1 -> "сбалансированная"
        else -> "стандартная"
    }
}

internal fun SettingsUiState.profileTitle(): String {
    return if (userId.isNullOrBlank()) "Защищённый профиль" else "Мой профиль"
}

internal fun SettingsUiState.profileSubtitle(): String {
    val rawDeviceId = deviceId?.trim().orEmpty()
    return if (rawDeviceId.isBlank()) {
        "Детали защищённой сессии появятся после входа."
    } else {
        "Текущее устройство ${rawDeviceId.takeLast(6)} защищено приватными настройками."
    }
}
