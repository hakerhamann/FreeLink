package com.freelink.core.model.domain

data class User(
    val id: String,
    val login: String,
    val displayName: String,
    val isOnline: Boolean
)
