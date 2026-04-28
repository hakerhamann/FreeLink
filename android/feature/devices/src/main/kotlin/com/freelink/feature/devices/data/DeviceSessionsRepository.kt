package com.freelink.feature.devices.data

import com.freelink.core.model.domain.auth.DeviceSession
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult

sealed interface DeviceSessionsResult<out T> {
    data class Success<T>(val value: T) : DeviceSessionsResult<T>
    data class Failure(val message: String) : DeviceSessionsResult<Nothing>
}

class DeviceSessionsRepository(
    private val authRepository: AuthRepository
) {
    suspend fun loadDevices(): DeviceSessionsResult<List<DeviceSession>> {
        return when (val result = authRepository.loadDevices()) {
            is AuthRepositoryResult.Success -> DeviceSessionsResult.Success(result.value)
            is AuthRepositoryResult.Failure -> DeviceSessionsResult.Failure(result.message)
        }
    }

    suspend fun revokeDevice(deviceId: String): DeviceSessionsResult<Unit> {
        return when (val result = authRepository.revokeDevice(deviceId)) {
            is AuthRepositoryResult.Success -> DeviceSessionsResult.Success(Unit)
            is AuthRepositoryResult.Failure -> DeviceSessionsResult.Failure(result.message)
        }
    }

    suspend fun logoutCurrentSession(): DeviceSessionsResult<Unit> {
        return when (val result = authRepository.logout()) {
            is AuthRepositoryResult.Success -> DeviceSessionsResult.Success(Unit)
            is AuthRepositoryResult.Failure -> DeviceSessionsResult.Failure(result.message)
        }
    }
}
