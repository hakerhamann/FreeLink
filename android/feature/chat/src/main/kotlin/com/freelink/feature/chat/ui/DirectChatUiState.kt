package com.freelink.feature.chat.ui

import com.freelink.feature.chat.ui.model.DirectMessageUiModel
import com.freelink.feature.chat.ui.model.ReplyTargetUiModel

data class DirectChatUiState(
    val chatId: String = "",
    val chatTitle: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val draft: String = "",
    val isPeerTyping: Boolean = false,
    val messages: List<DirectMessageUiModel> = emptyList(),
    val replyTarget: ReplyTargetUiModel? = null,
    val errorMessage: String? = null
)
