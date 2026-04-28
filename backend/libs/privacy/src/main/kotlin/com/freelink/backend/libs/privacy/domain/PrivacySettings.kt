package com.freelink.backend.libs.privacy.domain

data class PrivacySettings(
    val hiddenModeEnabled: Boolean,
    val biometricLockRequired: Boolean,
    val disappearingMessagesEnabled: Boolean,
    val linkPreviewEnabled: Boolean,
    val whoCanMessageMe: String
)
