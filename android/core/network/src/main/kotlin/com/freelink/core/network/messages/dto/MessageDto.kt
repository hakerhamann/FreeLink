package com.freelink.core.network.messages.dto

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
    val deliveryState: String,
    val envelope: MessageEnvelopeDto?
)
