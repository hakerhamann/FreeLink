package com.freelink.feature.chat.ui

import com.freelink.feature.chat.ui.model.DirectMessageUiModel

data class DirectChatUiState(
    val chatId: String = "",
    val chatTitle: String = "",
    val draft: String = "",
    val isPeerTyping: Boolean = false,
    val messages: List<DirectMessageUiModel> = emptyList()
)
