package com.freelink.feature.settings.ui

import com.freelink.feature.settings.ui.model.PrivacySettingsUiModel
import com.freelink.feature.settings.ui.model.WhoCanMessageUiOption
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsPresentationTest {

    @Test
    fun `privacyLevelLabel returns high for two hardening toggles`() {
        val state = SettingsUiState(
            settings = PrivacySettingsUiModel(
                hiddenModeEnabled = true,
                biometricLockRequired = true,
                disappearingMessagesEnabled = false,
                linkPreviewEnabled = true,
                whoCanMessageMe = WhoCanMessageUiOption.TRUSTED_CONTACTS
            )
        )

        assertEquals("высокая", state.privacyLevelLabel())
    }

    @Test
    fun `profileSubtitle uses device suffix when present`() {
        val state = SettingsUiState(deviceId = "device-abcdef")

        assertEquals(
            "Текущее устройство abcdef защищено приватными настройками.",
            state.profileSubtitle()
        )
    }
}
