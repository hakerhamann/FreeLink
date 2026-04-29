package com.freelink.backend.libs.messaging.service

import com.freelink.backend.libs.messaging.domain.ChatMessage
import com.freelink.backend.libs.messaging.model.EncryptedEnvelope

interface MessagingService {
    fun listMessages(
        userId: String,
        chatId: String,
        limit: Int?
    ): List<ChatMessage>

    fun sendMessage(
        userId: String,
        chatId: String,
        body: String,
        envelope: EncryptedEnvelope?
    ): ChatMessage
}
