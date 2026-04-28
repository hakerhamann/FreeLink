package com.freelink.core.model.domain

data class DeviceSession(
    val deviceId: String,
    val name: String,
    val platform: String,
    val lastSeenAtEpochMs: Long,
    val isCurrent: Boolean
)
