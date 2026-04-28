package com.freelink.backend.libs.chats.domain

enum class ChatType {
    DIRECT,
    GROUP
}

data class ChatSummary(
    val id: String,
    val title: String,
    val lastMessagePreview: String,
    val unreadCount: Int,
    val isPinned: Boolean,
    val updatedAtEpochMs: Long,
    val type: ChatType
)
