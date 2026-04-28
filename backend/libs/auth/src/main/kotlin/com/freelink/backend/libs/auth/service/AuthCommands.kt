package com.freelink.backend.libs.auth.service

data class RegisterCommand(
    val login: String,
    val password: String,
    val deviceName: String
)

data class LoginCommand(
    val login: String,
    val password: String,
    val deviceName: String
)

