package com.freelink.core.encryption

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TransportMessageEnvelopeFactoryTest {

    @Test
    fun `create builds versioned envelope with metadata`() {
        val fixedClock = Clock.fixed(Instant.parse("2026-04-29T10:15:30Z"), ZoneOffset.UTC)
        val factory = TransportMessageEnvelopeFactory(clock = fixedClock)

        val envelope = factory.create(
            conversationId = "chat-1",
            senderDeviceId = "device-1",
            plaintextBody = "hello"
        )

        assertEquals(1, envelope.version)
        assertEquals("chat-1", envelope.conversationId)
        assertEquals("device-1", envelope.senderDeviceId)
        assertEquals("2026-04-29T10:15:30Z", envelope.sentAtIso)
        assertNotEquals("hello", envelope.ciphertextBase64)
    }
}
