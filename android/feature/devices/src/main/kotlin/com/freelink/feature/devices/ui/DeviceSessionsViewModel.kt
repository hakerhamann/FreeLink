package com.freelink.feature.devices.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.devices.data.DeviceSessionsRepository
import com.freelink.feature.devices.data.DeviceSessionsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeviceSessionsViewModel(
    private val repository: DeviceSessionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceSessionsUiState(isLoading = true))
    val uiState: StateFlow<DeviceSessionsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.loadDevices()) {
                is DeviceSessionsResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sessions = result.value,
                            errorMessage = null
                        )
                    }
                }

                is DeviceSessionsResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun revokeDevice(deviceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(revokingDeviceId = deviceId, errorMessage = null) }

            when (val result = repository.revokeDevice(deviceId)) {
                is DeviceSessionsResult.Success -> refresh()
                is DeviceSessionsResult.Failure -> {
                    _uiState.update {
                        it.copy(revokingDeviceId = null, errorMessage = result.message)
                    }
                }
            }
        }
    }

    companion object {
        fun factory(repository: DeviceSessionsRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    DeviceSessionsViewModel(repository)
                }
            }
        }
    }
}
