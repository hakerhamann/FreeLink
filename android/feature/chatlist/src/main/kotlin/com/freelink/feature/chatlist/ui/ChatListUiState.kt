package com.freelink.feature.chatlist.ui

import com.freelink.feature.chatlist.ui.model.ChatListItemUiModel
import com.freelink.feature.chatlist.ui.model.ChatListPersonUiModel

data class ChatListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val unreadOnly: Boolean = false,
    val pinnedChats: List<ChatListItemUiModel> = emptyList(),
    val otherChats: List<ChatListItemUiModel> = emptyList(),
    val peopleMatches: List<ChatListPersonUiModel> = emptyList(),
    val errorMessage: String? = null,
    val emptyStateMessage: String? = null
)
