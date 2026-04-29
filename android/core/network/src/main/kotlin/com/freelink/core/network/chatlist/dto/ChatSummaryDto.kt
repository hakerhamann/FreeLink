package com.freelink.core.network.chatlist.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatSummaryDto(
    val id: String,
    val title: String,
    val lastMessagePreview: String,
    val type: String,
    val unreadCount: Int,
    val isPinned: Boolean,
    val updatedAtEpochMs: Long
)
