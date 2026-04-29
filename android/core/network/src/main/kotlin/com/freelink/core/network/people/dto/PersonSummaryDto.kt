package com.freelink.core.network.people.dto

import kotlinx.serialization.Serializable

@Serializable
data class PersonSummaryDto(
    val id: String,
    val login: String,
    val displayName: String,
    val isOnline: Boolean
)
