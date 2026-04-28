package com.freelink.core.database.chatlist.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_summaries")
data class ChatSummaryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessagePreview: String,
    val type: String,
    val unreadCount: Int,
    val isPinned: Boolean,
    val updatedAtEpochMs: Long
)
