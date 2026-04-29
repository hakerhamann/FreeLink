package com.freelink.backend.libs.messaging.service

import com.freelink.backend.libs.messaging.domain.MessageDeliveryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class InMemoryMessagingServiceTest {
    @Test
    fun sendMessageAddsNewItemForChat() {
        val service = InMemoryMessagingService()

        val before = service.listMessages(userId = "user-1", chatId = "chat-lera", limit = null)
        val replyTargetId = before.first().id
        val created = service.sendMessage(
            userId = "user-1",
            chatId = "chat-lera",
            body = "test message",
            envelope = null,
            replyToMessageId = replyTargetId
        )
        val after = service.listMessages(userId = "user-1", chatId = "chat-lera", limit = null)

        assertEquals(before.size + 1, after.size)
        assertEquals("test message", after.last().body)
        assertEquals(created.id, after.last().id)
        assertEquals(MessageDeliveryState.SENT, after.last().deliveryState)
        assertEquals(replyTargetId, after.last().replyToMessageId)
    }

    @Test
    fun listMessagesHonorsLatestLimit() {
        val service = InMemoryMessagingService()

        service.sendMessage(
            userId = "user-1",
            chatId = "chat-artem",
            body = "one",
            envelope = null,
            replyToMessageId = null
        )
        service.sendMessage(
            userId = "user-1",
            chatId = "chat-artem",
            body = "two",
            envelope = null,
            replyToMessageId = null
        )

        val limited = service.listMessages(userId = "user-1", chatId = "chat-artem", limit = 1)
        assertEquals(1, limited.size)
        assertEquals("two", limited.first().body)
    }

    @Test
    fun setReactionAggregatesAndReplacesPerUser() {
        val service = InMemoryMessagingService()
        val target = service.listMessages(userId = "user-1", chatId = "chat-lera", limit = null).first()

        val first = service.setReaction(
            userId = "user-1",
            chatId = "chat-lera",
            messageId = target.id,
            emoji = "👍"
        )
        assertNotNull(first)
        assertEquals(1, first?.reactions?.get("👍"))

        val second = service.setReaction(
            userId = "user-2",
            chatId = "chat-lera",
            messageId = target.id,
            emoji = "👍"
        )
        assertNotNull(second)
        assertEquals(2, second?.reactions?.get("👍"))

        val replaced = service.setReaction(
            userId = "user-1",
            chatId = "chat-lera",
            messageId = target.id,
            emoji = "🔥"
        )
        assertNotNull(replaced)
        assertEquals(1, replaced?.reactions?.get("👍"))
        assertEquals(1, replaced?.reactions?.get("🔥"))
    }

    @Test
    fun setReactionReturnsNullForUnknownMessage() {
        val service = InMemoryMessagingService()
        val result = service.setReaction(
            userId = "user-1",
            chatId = "chat-lera",
            messageId = "missing",
            emoji = "👍"
        )
        assertEquals(null, result)
    }
}
