package com.freelink.core.network.auth

import com.freelink.core.model.domain.auth.AuthSession
import com.freelink.core.model.domain.auth.DeviceSession

sealed interface AuthApiResult<out T> {
    data class Success<T>(val value: T) : AuthApiResult<T>
    data class Failure(val message: String, val statusCode: Int? = null) : AuthApiResult<Nothing>
}

interface AuthApiClient {
    suspend fun register(login: String, password: String, deviceName: String): AuthApiResult<AuthSession>
    suspend fun login(login: String, password: String, deviceName: String): AuthApiResult<AuthSession>
    suspend fun refresh(refreshToken: String): AuthApiResult<AuthSession>
    suspend fun logout(refreshToken: String): AuthApiResult<Unit>
    suspend fun listDevices(refreshToken: String): AuthApiResult<List<DeviceSession>>
}
