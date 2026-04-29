package com.freelink.core.network.messages.dto

data class MessageAttachmentDto(
    val id: String,
    val type: String,
    val fileName: String,
    val digestSha256: String,
    val byteSize: Long,
    val mimeType: String,
    val downloadUrl: String
)

data class MessageEnvelopeDto(
    val version: Int,
    val conversationId: String,
    val senderDeviceId: String,
    val ciphertext: String,
    val nonce: String,
    val sentAt: String
)

data class MessageDto(
    val id: String,
    val chatId: String,
    val senderUserId: String,
    val body: String,
    val createdAtEpochMs: Long,
    val expiresAtEpochMs: Long?,
    val deliveryState: String,
    val attachment: MessageAttachmentDto?,
    val envelope: MessageEnvelopeDto?,
    val replyToMessageId: String?,
    val reactions: Map<String, Int>
)
