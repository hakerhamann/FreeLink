package com.freelink.backend.libs.privacy.service

import com.freelink.backend.libs.privacy.domain.PrivacySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPrivacySettingsServiceTest {

    @Test
    fun returnsDefaultSettingsForNewUser() {
        val service = InMemoryPrivacySettingsService()

        val settings = service.getSettings("user-1")

        assertFalse(settings.hiddenModeEnabled)
        assertFalse(settings.biometricLockRequired)
        assertFalse(settings.disappearingMessagesEnabled)
        assertTrue(settings.linkPreviewEnabled)
        assertEquals("trusted_contacts", settings.whoCanMessageMe)
    }

    @Test
    fun updatePersistsAndSanitizesWhoCanMessage() {
        val service = InMemoryPrivacySettingsService()

        val updated = service.updateSettings(
            userId = "user-2",
            settings = PrivacySettings(
                hiddenModeEnabled = true,
                biometricLockRequired = true,
                disappearingMessagesEnabled = false,
                linkPreviewEnabled = false,
                whoCanMessageMe = "EVERYONE"
            )
        )

        assertTrue(updated.hiddenModeEnabled)
        assertTrue(updated.biometricLockRequired)
        assertFalse(updated.disappearingMessagesEnabled)
        assertFalse(updated.linkPreviewEnabled)
        assertEquals("everyone", updated.whoCanMessageMe)

        val fetched = service.getSettings("user-2")
        assertEquals(updated, fetched)
    }
}
