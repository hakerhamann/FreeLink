package com.freelink.feature.settings.ui.model

data class PrivacySettingsUiModel(
    val hiddenModeEnabled: Boolean = false,
    val biometricLockRequired: Boolean = false,
    val disappearingMessagesEnabled: Boolean = false,
    val linkPreviewEnabled: Boolean = true,
    val whoCanMessageMe: WhoCanMessageUiOption = WhoCanMessageUiOption.TRUSTED_CONTACTS
)

enum class WhoCanMessageUiOption(
    val backendValue: String,
    val title: String
) {
    EVERYONE(
        backendValue = "everyone",
        title = "Everyone"
    ),
    TRUSTED_CONTACTS(
        backendValue = "trusted_contacts",
        title = "Trusted only"
    );

    companion object {
        fun fromBackendValue(value: String): WhoCanMessageUiOption {
            return entries.firstOrNull { it.backendValue == value } ?: TRUSTED_CONTACTS
        }
    }
}
