package com.freelink.core.network.privacy

import com.freelink.core.model.domain.PrivacySettings
import com.freelink.core.network.auth.AuthApiResult

interface PrivacyApiClient {
    suspend fun getSettings(accessToken: String): AuthApiResult<PrivacySettings>
    suspend fun updateSettings(accessToken: String, settings: PrivacySettings): AuthApiResult<PrivacySettings>
}
