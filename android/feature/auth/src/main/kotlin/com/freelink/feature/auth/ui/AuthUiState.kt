package com.freelink.feature.auth.ui

data class AuthUiState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthorized: Boolean = false,
    val errorMessage: String? = null
)

