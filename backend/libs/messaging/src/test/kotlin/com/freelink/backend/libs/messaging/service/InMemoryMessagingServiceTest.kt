package com.freelink.backend.libs.messaging.service

import com.freelink.backend.libs.messaging.domain.MessageDeliveryState
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryMessagingServiceTest {
    @Test
    fun sendMessageAddsNewItemForChat() {
        val service = InMemoryMessagingService()

        val before = service.listMessages(userId = "user-1", chatId = "chat-lera", limit = null).size
        val created = service.sendMessage(
            userId = "user-1",
            chatId = "chat-lera",
            body = "Тестовое сообщение",
            envelope = null
        )
        val after = service.listMessages(userId = "user-1", chatId = "chat-lera", limit = null)

        assertEquals(before + 1, after.size)
        assertEquals("Тестовое сообщение", after.last().body)
        assertEquals(created.id, after.last().id)
        assertEquals(MessageDeliveryState.SENT, after.last().deliveryState)
    }

    @Test
    fun listMessagesHonorsLatestLimit() {
        val service = InMemoryMessagingService()

        service.sendMessage(
            userId = "user-1",
            chatId = "chat-artem",
            body = "one",
            envelope = null
        )
        service.sendMessage(
            userId = "user-1",
            chatId = "chat-artem",
            body = "two",
            envelope = null
        )

        val limited = service.listMessages(userId = "user-1", chatId = "chat-artem", limit = 1)
        assertEquals(1, limited.size)
        assertEquals("two", limited.first().body)
    }
}
