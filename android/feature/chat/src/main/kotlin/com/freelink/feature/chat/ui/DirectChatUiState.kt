package com.freelink.feature.chat.ui

import com.freelink.feature.chat.ui.model.DirectMessageUiModel

data class DirectChatUiState(
    val chatId: String = "",
    val chatTitle: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val draft: String = "",
    val messages: List<DirectMessageUiModel> = emptyList(),
    val errorMessage: String? = null
)
