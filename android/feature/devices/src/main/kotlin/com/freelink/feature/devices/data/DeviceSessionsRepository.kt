package com.freelink.feature.devices.data

import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.model.domain.auth.DeviceSession
import com.freelink.core.network.auth.AuthApiClient
import com.freelink.core.network.auth.AuthApiResult
import kotlinx.coroutines.flow.first

sealed interface DeviceSessionsResult<out T> {
    data class Success<T>(val value: T) : DeviceSessionsResult<T>
    data class Failure(val message: String) : DeviceSessionsResult<Nothing>
}

class DeviceSessionsRepository(
    private val authApiClient: AuthApiClient,
    private val authSessionStore: AuthSessionStore
) {
    suspend fun loadDevices(): DeviceSessionsResult<List<DeviceSession>> {
        val session = authSessionStore.sessionFlow.first()
            ?: return DeviceSessionsResult.Failure("Сессия не найдена. Выполните вход заново.")

        return when (val result = authApiClient.listDevices(session.refreshToken)) {
            is AuthApiResult.Success -> DeviceSessionsResult.Success(result.value)
            is AuthApiResult.Failure -> DeviceSessionsResult.Failure(result.message)
        }
    }

    suspend fun revokeDevice(deviceId: String): DeviceSessionsResult<Unit> {
        val session = authSessionStore.sessionFlow.first()
            ?: return DeviceSessionsResult.Failure("Сессия не найдена. Выполните вход заново.")

        return when (val result = authApiClient.revokeDevice(session.refreshToken, deviceId)) {
            is AuthApiResult.Success -> DeviceSessionsResult.Success(Unit)
            is AuthApiResult.Failure -> DeviceSessionsResult.Failure(result.message)
        }
    }

    suspend fun logoutCurrentSession(): DeviceSessionsResult<Unit> {
        val session = authSessionStore.sessionFlow.first()
            ?: return DeviceSessionsResult.Success(Unit)

        val result = authApiClient.logout(session.refreshToken)
        authSessionStore.clear()

        return when (result) {
            is AuthApiResult.Success -> DeviceSessionsResult.Success(Unit)
            is AuthApiResult.Failure -> DeviceSessionsResult.Failure(result.message)
        }
    }
}
