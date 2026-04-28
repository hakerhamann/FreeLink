package com.freelink.backend.libs.auth.service

import com.freelink.backend.libs.auth.domain.AuthTokens
import com.freelink.backend.libs.auth.domain.DeviceSession

interface AuthService {
    fun register(command: RegisterCommand): AuthTokens?
    fun login(command: LoginCommand): AuthTokens?
    fun refresh(refreshToken: String): AuthTokens?
    fun logout(refreshToken: String): Boolean
    fun listDevicesByAccessToken(accessToken: String): List<DeviceSession>?
    fun revokeDeviceByAccessToken(accessToken: String, deviceId: String): Boolean
    fun resolveUserIdByAccessToken(accessToken: String): String?
}

