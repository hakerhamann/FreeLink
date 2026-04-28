package com.freelink.feature.chatlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.chatlist.data.ChatListRepository
import com.freelink.feature.chatlist.data.ChatListResult
import com.freelink.feature.chatlist.ui.mapper.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val repository: ChatListRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val unreadOnly = MutableStateFlow(false)
    private var wsJob: Job? = null

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        observeChats()
        refresh()
        startRealtimeSync()
    }

    fun onSearchQueryChanged(value: String) {
        searchQuery.value = value
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun onUnreadOnlyChanged(value: Boolean) {
        unreadOnly.value = value
        _uiState.update { it.copy(unreadOnly = value) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            when (val result = repository.syncChats()) {
                is ChatListResult.Success -> {
                    _uiState.update { it.copy(isRefreshing = false, errorMessage = null) }
                }
                is ChatListResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun observeChats() {
        viewModelScope.launch {
            combine(
                repository.chatsFlow,
                searchQuery,
                unreadOnly
            ) { chats, query, unreadFilter ->
                chats.filter { chat ->
                    val matchedQuery = if (query.isBlank()) {
                        true
                    } else {
                        val normalizedQuery = query.trim().lowercase()
                        chat.title.lowercase().contains(normalizedQuery) ||
                            chat.lastMessagePreview.lowercase().contains(normalizedQuery)
                    }

                    val matchedUnread = if (unreadFilter) {
                        chat.unreadCount > 0
                    } else {
                        true
                    }

                    matchedQuery && matchedUnread
                }
            }.collect { filteredChats ->
                val mapped = filteredChats.map { it.toUiModel() }
                val pinned = mapped.filter { it.isPinned }
                val others = mapped.filterNot { it.isPinned }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pinnedChats = pinned,
                        otherChats = others,
                        emptyStateMessage = if (mapped.isEmpty()) "No chats found" else null
                    )
                }
            }
        }
    }

    private fun startRealtimeSync() {
        wsJob?.cancel()
        wsJob = viewModelScope.launch {
            repository.observeRemoteUpdates()
        }
    }

    override fun onCleared() {
        wsJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(repository: ChatListRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ChatListViewModel(repository)
                }
            }
        }
    }
}
