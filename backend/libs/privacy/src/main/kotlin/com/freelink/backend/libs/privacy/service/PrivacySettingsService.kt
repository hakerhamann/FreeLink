package com.freelink.backend.libs.privacy.service

import com.freelink.backend.libs.privacy.domain.PrivacySettings

interface PrivacySettingsService {
    fun getSettings(userId: String): PrivacySettings
    fun updateSettings(userId: String, settings: PrivacySettings): PrivacySettings
}
