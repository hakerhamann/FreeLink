package com.freelink.core.model.domain

enum class ChatType {
    DIRECT,
    GROUP
}

data class Chat(
    val id: String,
    val title: String,
    val type: ChatType,
    val unreadCount: Int,
    val isPinned: Boolean,
    val updatedAtEpochMs: Long
)
