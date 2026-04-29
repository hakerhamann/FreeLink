package com.freelink.core.encryption

import com.freelink.core.model.domain.MessageEnvelope
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.util.Base64

class TransportMessageEnvelopeFactory(
    private val clock: Clock = Clock.systemUTC(),
    private val secureRandom: SecureRandom = SecureRandom()
) : MessageEnvelopeFactory {

    override fun create(
        conversationId: String,
        senderDeviceId: String,
        plaintextBody: String
    ): MessageEnvelope {
        require(conversationId.isNotBlank()) { "conversationId must not be blank." }
        require(senderDeviceId.isNotBlank()) { "senderDeviceId must not be blank." }

        return MessageEnvelope(
            version = ENVELOPE_VERSION,
            conversationId = conversationId,
            senderDeviceId = senderDeviceId,
            ciphertextBase64 = plaintextBody.toBase64(),
            nonceBase64 = nextNonceBase64(),
            sentAtIso = Instant.now(clock).toString()
        )
    }

    private fun nextNonceBase64(): String {
        val nonce = ByteArray(NONCE_SIZE_BYTES)
        secureRandom.nextBytes(nonce)
        return Base64.getEncoder().withoutPadding().encodeToString(nonce)
    }

    private fun String.toBase64(): String {
        return Base64.getEncoder()
            .withoutPadding()
            .encodeToString(toByteArray(Charsets.UTF_8))
    }

    private companion object {
        const val ENVELOPE_VERSION = 1
        const val NONCE_SIZE_BYTES = 12
    }
}
