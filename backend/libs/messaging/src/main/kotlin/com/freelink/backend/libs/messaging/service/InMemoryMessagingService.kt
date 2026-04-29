package com.freelink.backend.libs.messaging.service

import com.freelink.backend.libs.messaging.domain.ChatMessage
import com.freelink.backend.libs.messaging.domain.MessageDeliveryState
import com.freelink.backend.libs.messaging.model.EncryptedEnvelope
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryMessagingService : MessagingService {
    private val messagesByChatId = ConcurrentHashMap<String, MutableList<ChatMessage>>()
    private val reactionsByMessageId = ConcurrentHashMap<String, MutableMap<String, String>>()

    override fun listMessages(userId: String, chatId: String, limit: Int?): List<ChatMessage> {
        val source = messagesByChatId.computeIfAbsent(chatId) { createSeedMessages(chatId) }
        val snapshot = synchronized(source) { source.toList() }.sortedBy { it.createdAtEpochMs }

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
        envelope: EncryptedEnvelope?,
        replyToMessageId: String?
    ): ChatMessage {
        val source = messagesByChatId.computeIfAbsent(chatId) { createSeedMessages(chatId) }
        val replyId = replyToMessageId?.takeIf { targetId ->
            synchronized(source) { source.any { it.id == targetId } }
        }

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUserId = userId,
            body = body,
            createdAtEpochMs = System.currentTimeMillis(),
            deliveryState = MessageDeliveryState.SENT,
            envelope = envelope,
            replyToMessageId = replyId
        )

        synchronized(source) {
            source.add(message)
        }
        return message
    }

    override fun setReaction(
        userId: String,
        chatId: String,
        messageId: String,
        emoji: String
    ): ChatMessage? {
        val source = messagesByChatId.computeIfAbsent(chatId) { createSeedMessages(chatId) }
        synchronized(source) {
            val index = source.indexOfFirst { it.id == messageId }
            if (index == -1) {
                return null
            }

            val byUser = reactionsByMessageId.computeIfAbsent(messageId) {
                ConcurrentHashMap()
            }
            byUser[userId] = emoji

            val aggregate = buildAggregate(byUser)
            val updated = source[index].copy(reactions = aggregate)
            source[index] = updated
            return updated
        }
    }

    private fun buildAggregate(byUser: Map<String, String>): Map<String, Int> {
        return byUser.values
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .toMap(linkedMapOf())
    }

    private fun createSeedMessages(chatId: String): MutableList<ChatMessage> {
        val now = System.currentTimeMillis()
        val first = ChatMessage(
            id = "$chatId-seed-1",
            chatId = chatId,
            senderUserId = "peer",
            body = "Hey, are you online?",
            createdAtEpochMs = now - 120_000,
            deliveryState = MessageDeliveryState.READ
        )
        val second = ChatMessage(
            id = "$chatId-seed-2",
            chatId = chatId,
            senderUserId = "me",
            body = "Yes, here.",
            createdAtEpochMs = now - 60_000,
            deliveryState = MessageDeliveryState.READ,
            replyToMessageId = first.id
        )

        return Collections.synchronizedList(mutableListOf(first, second))
    }
}
