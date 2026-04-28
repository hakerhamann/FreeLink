package com.freelink.backend.libs.messaging.model

import kotlinx.serialization.Serializable

@Serializable
data class EncryptedEnvelope(
    val version: Int,
    val conversationId: String,
    val senderDeviceId: String,
    val ciphertext: String,
    val nonce: String,
    val sentAt: String
)
