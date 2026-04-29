package com.freelink.feature.settings.ui.mapper

import com.freelink.core.model.domain.PrivacySettings
import com.freelink.feature.settings.ui.model.PrivacySettingsUiModel
import com.freelink.feature.settings.ui.model.WhoCanMessageUiOption

fun PrivacySettings.toUiModel(): PrivacySettingsUiModel {
    return PrivacySettingsUiModel(
        hiddenModeEnabled = hiddenModeEnabled,
        biometricLockRequired = biometricLockRequired,
        disappearingMessagesEnabled = disappearingMessagesEnabled,
        linkPreviewEnabled = linkPreviewEnabled,
        whoCanMessageMe = WhoCanMessageUiOption.fromBackendValue(whoCanMessageMe)
    )
}

fun PrivacySettingsUiModel.toDomain(): PrivacySettings {
    return PrivacySettings(
        hiddenModeEnabled = hiddenModeEnabled,
        biometricLockRequired = biometricLockRequired,
        disappearingMessagesEnabled = disappearingMessagesEnabled,
        linkPreviewEnabled = linkPreviewEnabled,
        whoCanMessageMe = whoCanMessageMe.backendValue
    )
}
