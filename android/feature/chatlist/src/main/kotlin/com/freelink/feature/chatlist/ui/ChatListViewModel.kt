package com.freelink.feature.chatlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.core.model.domain.Chat
import com.freelink.core.model.domain.User
import com.freelink.feature.chatlist.data.ChatListRepository
import com.freelink.feature.chatlist.data.ChatListResult
import com.freelink.feature.chatlist.ui.mapper.toChatListPersonUiModel
import com.freelink.feature.chatlist.ui.mapper.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
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
            val chatSync = repository.syncChats()
            val peopleSync = repository.syncPeople()
            val failures = buildList {
                if (chatSync is ChatListResult.Failure) {
                    add(chatSync.message)
                }
                if (peopleSync is ChatListResult.Failure) {
                    add(peopleSync.message)
                }
            }.distinct()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = failures.firstOrNull()
                )
            }
        }
    }

    fun archiveChat(chatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (val result = repository.archiveChat(chatId)) {
                is ChatListResult.Success -> Unit
                is ChatListResult.Failure -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    private fun observeChats() {
        viewModelScope.launch {
            combine(
                repository.chatsFlow,
                repository.peopleFlow,
                searchQuery,
                unreadOnly
            ) { chats, people, query, unreadFilter ->
                val normalizedQuery = query.trim().lowercase()

                val filteredChats = chats.filter { chat ->
                    val matchedQuery = if (query.isBlank()) {
                        true
                    } else {
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

                val filteredPeople = if (normalizedQuery.isBlank()) {
                    emptyList()
                } else {
                    people.filter { person ->
                        person.displayName.lowercase().contains(normalizedQuery) ||
                            person.login.lowercase().contains(normalizedQuery)
                    }
                }

                SearchResult(
                    chats = filteredChats,
                    people = filteredPeople,
                    hasQuery = normalizedQuery.isNotBlank()
                )
            }.collect { result ->
                val mappedChats = result.chats.map { it.toUiModel() }
                val pinned = mappedChats.filter { it.isPinned }
                val others = mappedChats.filterNot { it.isPinned }
                val mappedPeople = result.people.map { it.toChatListPersonUiModel() }
                val noResults = mappedChats.isEmpty() && mappedPeople.isEmpty()
                val emptyStateMessage = if (noResults) {
                    if (result.hasQuery) "No chats or people found" else "No chats found"
                } else {
                    null
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pinnedChats = pinned,
                        otherChats = others,
                        peopleMatches = mappedPeople,
                        emptyStateMessage = emptyStateMessage
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

    private data class SearchResult(
        val chats: List<Chat>,
        val people: List<User>,
        val hasQuery: Boolean
    )
}
