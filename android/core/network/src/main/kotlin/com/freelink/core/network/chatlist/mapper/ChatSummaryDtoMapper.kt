package com.freelink.core.network.chatlist.mapper

import com.freelink.core.model.domain.Chat
import com.freelink.core.model.domain.ChatType
import com.freelink.core.network.chatlist.dto.ChatSummaryDto

fun ChatSummaryDto.toDomain(): Chat {
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
