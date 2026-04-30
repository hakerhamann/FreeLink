package com.freelink.core.datastore.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.freelink.core.model.domain.auth.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class AuthSessionStore(
    private val dataStore: DataStore<Preferences>
) {

    val sessionFlow: Flow<AuthSession?> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            val accessToken = preferences[Keys.accessToken] ?: return@map null
            val refreshToken = preferences[Keys.refreshToken] ?: return@map null
            val userId = preferences[Keys.userId] ?: return@map null
            val deviceId = preferences[Keys.deviceId] ?: return@map null
            val expiresInSeconds = preferences[Keys.expiresInSeconds] ?: 0L

            AuthSession(
                userId = userId,
                deviceId = deviceId,
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresInSeconds = expiresInSeconds
            )
        }

    suspend fun save(session: AuthSession) {
        dataStore.edit { preferences ->
            preferences[Keys.userId] = session.userId
            preferences[Keys.deviceId] = session.deviceId
            preferences[Keys.accessToken] = session.accessToken
            preferences[Keys.refreshToken] = session.refreshToken
            preferences[Keys.expiresInSeconds] = session.expiresInSeconds
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var instance: AuthSessionStore? = null

        private object Keys {
            val userId = stringPreferencesKey("auth_user_id")
            val deviceId = stringPreferencesKey("auth_device_id")
            val accessToken = stringPreferencesKey("auth_access_token")
            val refreshToken = stringPreferencesKey("auth_refresh_token")
            val expiresInSeconds = longPreferencesKey("auth_expires_in_seconds")
        }

        fun create(context: Context): AuthSessionStore {
            return instance ?: synchronized(this) {
                instance ?: buildStore(context.applicationContext).also { store ->
                    instance = store
                }
            }
        }

        private fun buildStore(context: Context): AuthSessionStore {
            return AuthSessionStore(
                PreferenceDataStoreFactory.create {
                    context.preferencesDataStoreFile("freelink_auth_preferences")
                }
            )
        }
    }
}
