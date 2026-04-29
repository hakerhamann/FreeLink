package com.freelink.core.network.messages.mapper

import com.freelink.core.model.domain.Message
import com.freelink.core.model.domain.MessageDeliveryState
import com.freelink.core.model.domain.MessageEnvelope
import com.freelink.core.network.messages.dto.MessageDto

fun MessageDto.toDomain(): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderUserId = senderUserId,
        body = body,
        createdAtEpochMs = createdAtEpochMs,
        deliveryState = MessageDeliveryState.entries.firstOrNull { it.name == deliveryState }
            ?: MessageDeliveryState.SENT,
        envelope = envelope?.let {
            MessageEnvelope(
                version = it.version,
                conversationId = it.conversationId,
                senderDeviceId = it.senderDeviceId,
                ciphertextBase64 = it.ciphertext,
                nonceBase64 = it.nonce,
                sentAtIso = it.sentAt
            )
        }
    )
}
