package com.freelink.core.model.domain.auth

data class AuthSession(
    val userId: String,
    val deviceId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long
)

data class DeviceSession(
    val deviceId: String,
    val deviceName: String,
    val lastSeenAtIso: String,
    val isCurrent: Boolean
)
