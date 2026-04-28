package com.freelink.backend.libs.auth.domain

data class AuthTokens(
    val userId: String,
    val deviceId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long
)

