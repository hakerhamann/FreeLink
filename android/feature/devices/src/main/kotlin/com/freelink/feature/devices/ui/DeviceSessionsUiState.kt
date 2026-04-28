package com.freelink.feature.devices.ui

import com.freelink.core.model.domain.auth.DeviceSession

data class DeviceSessionsUiState(
    val isLoading: Boolean = false,
    val sessions: List<DeviceSession> = emptyList(),
    val errorMessage: String? = null,
    val revokingDeviceId: String? = null
)
