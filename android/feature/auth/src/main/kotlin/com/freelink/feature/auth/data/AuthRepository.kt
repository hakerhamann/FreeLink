package com.freelink.feature.auth.data

import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.model.domain.auth.AuthSession
import com.freelink.core.model.domain.auth.DeviceSession
import com.freelink.core.network.auth.AuthApiClient
import com.freelink.core.network.auth.AuthApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

sealed interface AuthRepositoryResult<out T> {
    data class Success<T>(val value: T) : AuthRepositoryResult<T>
    data class Failure(val message: String) : AuthRepositoryResult<Nothing>
}

class AuthRepository(
    private val apiClient: AuthApiClient,
    private val sessionStore: AuthSessionStore
) {

    val sessionFlow: Flow<AuthSession?> = sessionStore.sessionFlow

    suspend fun login(login: String, password: String): AuthRepositoryResult<AuthSession> {
        return when (
            val result = apiClient.login(
                login = login,
                password = password,
                deviceName = "Android Device"
            )
        ) {
            is AuthApiResult.Success -> {
                sessionStore.save(result.value)
                AuthRepositoryResult.Success(result.value)
            }
            is AuthApiResult.Failure -> AuthRepositoryResult.Failure(result.message)
        }
    }

    suspend fun logout(): AuthRepositoryResult<Unit> {
        val refreshToken = sessionStore.sessionFlow.first()?.refreshToken
            ?: return AuthRepositoryResult.Success(Unit)

        val result = apiClient.logout(refreshToken)
        sessionStore.clear()

        return when (result) {
            is AuthApiResult.Success -> AuthRepositoryResult.Success(Unit)
            is AuthApiResult.Failure -> AuthRepositoryResult.Failure(result.message)
        }
    }

    suspend fun loadDevices(): AuthRepositoryResult<List<DeviceSession>> {
        val refreshToken = sessionStore.sessionFlow.first()?.refreshToken
            ?: return AuthRepositoryResult.Success(emptyList())

        return when (val result = apiClient.listDevices(refreshToken)) {
            is AuthApiResult.Success -> AuthRepositoryResult.Success(result.value)
            is AuthApiResult.Failure -> AuthRepositoryResult.Failure(result.message)
        }
    }
}
