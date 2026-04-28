package com.freelink.backend.libs.auth.domain

data class DeviceSession(
    val deviceId: String,
    val deviceName: String,
    val lastSeenAtIso: String,
    val isCurrent: Boolean
)

