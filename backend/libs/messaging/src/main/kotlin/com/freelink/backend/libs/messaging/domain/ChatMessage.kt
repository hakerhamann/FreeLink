package com.freelink.backend.libs.messaging.domain

import com.freelink.backend.libs.messaging.model.EncryptedEnvelope

data class ChatMessage(
    val id: String,
    val chatId: String,
    val senderUserId: String,
    val body: String,
    val createdAtEpochMs: Long,
    val deliveryState: MessageDeliveryState,
    val attachment: MessageAttachment? = null,
    val envelope: EncryptedEnvelope? = null,
    val replyToMessageId: String? = null,
    val reactions: Map<String, Int> = emptyMap()
)
