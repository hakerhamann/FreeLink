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

    suspend fun register(login: String, password: String): AuthRepositoryResult<AuthSession> {
        return when (
            val result = apiClient.register(
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
        return withAutoRefresh { session ->
            apiClient.listDevices(session.accessToken)
        }
    }

    suspend fun revokeDevice(deviceId: String): AuthRepositoryResult<Unit> {
        return withAutoRefresh { session ->
            apiClient.revokeDevice(
                accessToken = session.accessToken,
                deviceId = deviceId
            )
        }
    }

    suspend fun <T> authorizedRequest(
        request: suspend (session: AuthSession) -> AuthApiResult<T>
    ): AuthRepositoryResult<T> {
        return withAutoRefresh(request)
    }

    private suspend fun <T> withAutoRefresh(
        request: suspend (session: AuthSession) -> AuthApiResult<T>
    ): AuthRepositoryResult<T> {
        val currentSession = sessionStore.sessionFlow.first()
            ?: return AuthRepositoryResult.Failure("Сессия не найдена. Выполните вход заново.")

        val firstAttempt = request(currentSession)
        if (firstAttempt is AuthApiResult.Success) {
            return AuthRepositoryResult.Success(firstAttempt.value)
        }

        val firstFailure = firstAttempt as AuthApiResult.Failure
        if (firstFailure.statusCode != 401) {
            return AuthRepositoryResult.Failure(firstFailure.message)
        }

        val refreshedSession = refreshSession(currentSession)
            ?: return AuthRepositoryResult.Failure("Сессия истекла. Выполните вход заново.")

        return when (val secondAttempt = request(refreshedSession)) {
            is AuthApiResult.Success -> AuthRepositoryResult.Success(secondAttempt.value)
            is AuthApiResult.Failure -> AuthRepositoryResult.Failure(secondAttempt.message)
        }
    }

    private suspend fun refreshSession(session: AuthSession): AuthSession? {
        return when (val refreshResult = apiClient.refresh(session.refreshToken)) {
            is AuthApiResult.Success -> {
                sessionStore.save(refreshResult.value)
                refreshResult.value
            }
            is AuthApiResult.Failure -> {
                sessionStore.clear()
                null
            }
        }
    }
}
