package com.freelink.core.model.domain

enum class MessageDeliveryState {
    SENT,
    DELIVERED,
    READ
}

data class MessageEnvelope(
    val version: Int,
    val conversationId: String,
    val senderDeviceId: String,
    val ciphertextBase64: String,
    val nonceBase64: String,
    val sentAtIso: String
)

data class Message(
    val id: String,
    val chatId: String,
    val senderUserId: String,
    val body: String,
    val createdAtEpochMs: Long,
    val expiresAtEpochMs: Long? = null,
    val deliveryState: MessageDeliveryState,
    val attachment: MediaAttachment? = null,
    val envelope: MessageEnvelope? = null,
    val replyToMessageId: String? = null,
    val reactions: Map<String, Int> = emptyMap()
)
