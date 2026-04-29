package com.freelink.backend.libs.privacy.service

import com.freelink.backend.libs.privacy.domain.PrivacySettings
import java.util.concurrent.ConcurrentHashMap

class InMemoryPrivacySettingsService : PrivacySettingsService {
    private val settingsByUserId = ConcurrentHashMap<String, PrivacySettings>()

    override fun getSettings(userId: String): PrivacySettings {
        return settingsByUserId.computeIfAbsent(userId) {
            defaultPrivacySettings()
        }
    }

    override fun updateSettings(userId: String, settings: PrivacySettings): PrivacySettings {
        settingsByUserId[userId] = sanitize(settings)
        return settingsByUserId[userId] ?: defaultPrivacySettings()
    }

    private fun sanitize(settings: PrivacySettings): PrivacySettings {
        val normalizedWhoCanMessage = when (settings.whoCanMessageMe.lowercase()) {
            "everyone", "trusted_contacts" -> settings.whoCanMessageMe.lowercase()
            else -> "trusted_contacts"
        }

        return settings.copy(whoCanMessageMe = normalizedWhoCanMessage)
    }

    private fun defaultPrivacySettings(): PrivacySettings {
        return PrivacySettings(
            hiddenModeEnabled = false,
            biometricLockRequired = false,
            disappearingMessagesEnabled = false,
            linkPreviewEnabled = true,
            whoCanMessageMe = "trusted_contacts"
        )
    }
}
