package com.freelink.core.network.privacy.mapper

import com.freelink.core.model.domain.PrivacySettings
import com.freelink.core.network.privacy.dto.PrivacySettingsDto

fun PrivacySettingsDto.toDomain(): PrivacySettings {
    return PrivacySettings(
        hiddenModeEnabled = hiddenModeEnabled,
        biometricLockRequired = biometricLockRequired,
        disappearingMessagesEnabled = disappearingMessagesEnabled,
        linkPreviewEnabled = linkPreviewEnabled,
        whoCanMessageMe = whoCanMessageMe
    )
}

fun PrivacySettings.toDto(): PrivacySettingsDto {
    return PrivacySettingsDto(
        hiddenModeEnabled = hiddenModeEnabled,
        biometricLockRequired = biometricLockRequired,
        disappearingMessagesEnabled = disappearingMessagesEnabled,
        linkPreviewEnabled = linkPreviewEnabled,
        whoCanMessageMe = whoCanMessageMe
    )
}
