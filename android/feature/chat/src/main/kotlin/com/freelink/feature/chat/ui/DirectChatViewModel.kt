package com.freelink.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.chat.data.DirectChatRepository
import com.freelink.feature.chat.data.DirectChatResult
import com.freelink.feature.chat.ui.mapper.toDirectUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DirectChatViewModel(
    private val chatId: String,
    chatTitle: String,
    private val repository: DirectChatRepository
) : ViewModel() {
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow(
        DirectChatUiState(
            chatId = chatId,
            chatTitle = chatTitle.ifBlank { "Direct chat" }
        )
    )
    val uiState: StateFlow<DirectChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentUserId = repository.currentUserId()
            refreshMessages(isInitial = true)
        }
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshMessages(isInitial = false)
        }
    }

    fun sendMessage() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (val result = repository.sendMessage(chatId = chatId, body = text)) {
                is DirectChatResult.Success -> {
                    _uiState.update { it.copy(draft = "") }
                    refreshMessages(isInitial = false)
                }
                is DirectChatResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private suspend fun refreshMessages(isInitial: Boolean) {
        _uiState.update { state ->
            state.copy(
                isLoading = if (isInitial) true else state.isLoading,
                isRefreshing = !isInitial,
                errorMessage = null
            )
        }

        when (val result = repository.loadMessages(chatId)) {
            is DirectChatResult.Success -> {
                val mapped = result.value
                    .sortedBy { it.createdAtEpochMs }
                    .map { it.toDirectUiModel(currentUserId = currentUserId) }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        messages = mapped,
                        errorMessage = null
                    )
                }
            }
            is DirectChatResult.Failure -> {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            chatId: String,
            chatTitle: String,
            repository: DirectChatRepository
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    DirectChatViewModel(
                        chatId = chatId,
                        chatTitle = chatTitle,
                        repository = repository
                    )
                }
            }
        }
    }
}
