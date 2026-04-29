package com.freelink.backend.libs.people.domain

data class PersonSummary(
    val id: String,
    val login: String,
    val displayName: String,
    val isOnline: Boolean
)
