package com.freelink.feature.archive.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.archive.data.ArchiveRepository
import com.freelink.feature.archive.data.ArchiveResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArchiveViewModel(
    private val repository: ArchiveRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.loadArchivedChats()) {
                is ArchiveResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, chats = result.value, errorMessage = null)
                    }
                }
                is ArchiveResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    companion object {
        fun factory(repository: ArchiveRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ArchiveViewModel(repository)
                }
            }
        }
    }
}
