package com.freelink.core.network.privacy.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrivacySettingsDto(
    val hiddenModeEnabled: Boolean,
    val biometricLockRequired: Boolean,
    val disappearingMessagesEnabled: Boolean,
    val linkPreviewEnabled: Boolean,
    val whoCanMessageMe: String
)
