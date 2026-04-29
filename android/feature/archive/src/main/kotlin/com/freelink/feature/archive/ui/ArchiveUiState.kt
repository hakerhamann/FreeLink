package com.freelink.feature.archive.ui

import com.freelink.core.model.domain.Chat

data class ArchiveUiState(
    val isLoading: Boolean = true,
    val chats: List<Chat> = emptyList(),
    val errorMessage: String? = null
)
