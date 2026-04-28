package com.freelink.feature.chatlist.ui.model

data class ChatListItemUiModel(
    val id: String,
    val title: String,
    val lastMessagePreview: String,
    val unreadCount: Int,
    val unreadBadge: String?,
    val isPinned: Boolean,
    val updatedAtLabel: String,
    val chatTypeLabel: String
)
