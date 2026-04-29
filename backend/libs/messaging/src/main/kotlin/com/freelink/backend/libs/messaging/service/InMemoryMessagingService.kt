package com.freelink.backend.libs.messaging.service

import com.freelink.backend.libs.messaging.domain.ChatMessage
import com.freelink.backend.libs.messaging.domain.MessageDeliveryState
import com.freelink.backend.libs.messaging.model.EncryptedEnvelope
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryMessagingService : MessagingService {
    private val messagesByChatId = ConcurrentHashMap<String, MutableList<ChatMessage>>()

    override fun listMessages(userId: String, chatId: String, limit: Int?): List<ChatMessage> {
        val source = messagesByChatId.computeIfAbsent(chatId) { createSeedMessages(chatId) }
        val snapshot = synchronized(source) { source.toList() }
            .sortedBy { it.createdAtEpochMs }

        return if (limit == null || limit <= 0 || limit >= snapshot.size) {
            snapshot
        } else {
            snapshot.takeLast(limit)
        }
    }

    override fun sendMessage(
        userId: String,
        chatId: String,
        body: String,
        envelope: EncryptedEnvelope?
    ): ChatMessage {
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUserId = userId,
            body = body,
            createdAtEpochMs = System.currentTimeMillis(),
            deliveryState = MessageDeliveryState.SENT,
            envelope = envelope
        )

        val source = messagesByChatId.computeIfAbsent(chatId) { createSeedMessages(chatId) }
        synchronized(source) {
            source.add(message)
        }
        return message
    }

    private fun createSeedMessages(chatId: String): MutableList<ChatMessage> {
        val now = System.currentTimeMillis()
        return Collections.synchronizedList(
            mutableListOf(
                ChatMessage(
                    id = "$chatId-seed-1",
                    chatId = chatId,
                    senderUserId = "peer",
                    body = "Привет! На связи.",
                    createdAtEpochMs = now - 120_000,
                    deliveryState = MessageDeliveryState.READ
                ),
                ChatMessage(
                    id = "$chatId-seed-2",
                    chatId = chatId,
                    senderUserId = "me",
                    body = "Отлично, как ты?",
                    createdAtEpochMs = now - 60_000,
                    deliveryState = MessageDeliveryState.READ
                )
            )
        )
    }
}
