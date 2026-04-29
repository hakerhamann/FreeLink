package com.freelink.core.database.chatlist.mapper

import com.freelink.core.database.chatlist.entity.ChatSummaryEntity
import com.freelink.core.model.domain.Chat
import com.freelink.core.model.domain.ChatType

fun ChatSummaryEntity.toDomain(): Chat {
    return Chat(
        id = id,
        title = title,
        lastMessagePreview = lastMessagePreview,
        type = ChatType.entries.firstOrNull { it.name == type } ?: ChatType.DIRECT,
        unreadCount = unreadCount,
        isPinned = isPinned,
        updatedAtEpochMs = updatedAtEpochMs
    )
}

fun Chat.toEntity(): ChatSummaryEntity {
    return ChatSummaryEntity(
        id = id,
        title = title,
        lastMessagePreview = lastMessagePreview,
        type = type.name,
        unreadCount = unreadCount,
        isPinned = isPinned,
        updatedAtEpochMs = updatedAtEpochMs
    )
}
