package com.freelink.core.datastore.privacy

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.freelink.core.model.domain.PrivacySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PrivacySettingsStore(
    private val dataStore: DataStore<Preferences>
) {

    val settingsFlow: Flow<PrivacySettings> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            PrivacySettings(
                hiddenModeEnabled = preferences[Keys.hiddenModeEnabled] ?: false,
                biometricLockRequired = preferences[Keys.biometricLockRequired] ?: false,
                disappearingMessagesEnabled = preferences[Keys.disappearingMessagesEnabled] ?: false,
                linkPreviewEnabled = preferences[Keys.linkPreviewEnabled] ?: true,
                whoCanMessageMe = preferences[Keys.whoCanMessageMe] ?: "trusted_contacts"
            )
        }

    suspend fun save(settings: PrivacySettings) {
        dataStore.edit { preferences ->
            preferences[Keys.hiddenModeEnabled] = settings.hiddenModeEnabled
            preferences[Keys.biometricLockRequired] = settings.biometricLockRequired
            preferences[Keys.disappearingMessagesEnabled] = settings.disappearingMessagesEnabled
            preferences[Keys.linkPreviewEnabled] = settings.linkPreviewEnabled
            preferences[Keys.whoCanMessageMe] = settings.whoCanMessageMe
        }
    }

    companion object {
        @Volatile
        private var instance: PrivacySettingsStore? = null

        private object Keys {
            val hiddenModeEnabled = booleanPreferencesKey("privacy_hidden_mode_enabled")
            val biometricLockRequired = booleanPreferencesKey("privacy_biometric_lock_required")
            val disappearingMessagesEnabled = booleanPreferencesKey("privacy_disappearing_messages_enabled")
            val linkPreviewEnabled = booleanPreferencesKey("privacy_link_preview_enabled")
            val whoCanMessageMe = stringPreferencesKey("privacy_who_can_message_me")
        }

        fun create(context: Context): PrivacySettingsStore {
            return instance ?: synchronized(this) {
                instance ?: buildStore(context.applicationContext).also { store ->
                    instance = store
                }
            }
        }

        private fun buildStore(context: Context): PrivacySettingsStore {
            return PrivacySettingsStore(
                PreferenceDataStoreFactory.create {
                    context.preferencesDataStoreFile("freelink_privacy_preferences")
                }
            )
        }
    }
}
