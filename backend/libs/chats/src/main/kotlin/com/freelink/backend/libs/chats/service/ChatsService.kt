package com.freelink.backend.libs.chats.service

import com.freelink.backend.libs.chats.domain.ChatSummary

interface ChatsService {
    fun listChats(
        userId: String,
        query: String?,
        unreadOnly: Boolean
    ): List<ChatSummary>

    fun archiveChat(
        userId: String,
        chatId: String
    ): Boolean
}
