package com.freelink.feature.settings.data

import com.freelink.core.datastore.privacy.PrivacySettingsStore
import com.freelink.core.model.domain.PrivacySettings
import com.freelink.core.network.privacy.PrivacyApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult
import kotlinx.coroutines.flow.Flow

sealed interface PrivacySettingsResult<out T> {
    data class Success<T>(val value: T) : PrivacySettingsResult<T>
    data class Failure(val message: String) : PrivacySettingsResult<Nothing>
}

class PrivacySettingsRepository(
    private val authRepository: AuthRepository,
    private val privacyApiClient: PrivacyApiClient,
    private val settingsStore: PrivacySettingsStore
) {
    val settingsFlow: Flow<PrivacySettings> = settingsStore.settingsFlow

    suspend fun refreshSettings(): PrivacySettingsResult<PrivacySettings> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                privacyApiClient.getSettings(session.accessToken)
            }
        ) {
            is AuthRepositoryResult.Success -> {
                settingsStore.save(result.value)
                PrivacySettingsResult.Success(result.value)
            }
            is AuthRepositoryResult.Failure -> PrivacySettingsResult.Failure(result.message)
        }
    }

    suspend fun updateSettings(settings: PrivacySettings): PrivacySettingsResult<PrivacySettings> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                privacyApiClient.updateSettings(
                    accessToken = session.accessToken,
                    settings = settings
                )
            }
        ) {
            is AuthRepositoryResult.Success -> {
                settingsStore.save(result.value)
                PrivacySettingsResult.Success(result.value)
            }
            is AuthRepositoryResult.Failure -> PrivacySettingsResult.Failure(result.message)
        }
    }

    suspend fun logout(): PrivacySettingsResult<Unit> {
        return when (val result = authRepository.logout()) {
            is AuthRepositoryResult.Success -> PrivacySettingsResult.Success(Unit)
            is AuthRepositoryResult.Failure -> PrivacySettingsResult.Failure(result.message)
        }
    }
}
