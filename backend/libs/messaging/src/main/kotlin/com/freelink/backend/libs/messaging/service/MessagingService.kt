package com.freelink.backend.libs.messaging.service

import com.freelink.backend.libs.messaging.domain.ChatMessage
import com.freelink.backend.libs.messaging.domain.MessageAttachment
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
        attachment: MessageAttachment?,
        envelope: EncryptedEnvelope?,
        replyToMessageId: String?
    ): ChatMessage

    fun setReaction(
        userId: String,
        chatId: String,
        messageId: String,
        emoji: String
    ): ChatMessage?
}
