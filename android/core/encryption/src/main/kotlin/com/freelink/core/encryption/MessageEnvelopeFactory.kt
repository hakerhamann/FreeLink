package com.freelink.core.encryption

import com.freelink.core.model.domain.MessageEnvelope

interface MessageEnvelopeFactory {
    fun create(
        conversationId: String,
        senderDeviceId: String,
        plaintextBody: String
    ): MessageEnvelope
}
