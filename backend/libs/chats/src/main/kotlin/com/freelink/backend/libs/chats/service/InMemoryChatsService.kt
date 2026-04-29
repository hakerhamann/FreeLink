package com.freelink.backend.libs.chats.service

import com.freelink.backend.libs.chats.domain.ChatSummary
import com.freelink.backend.libs.chats.domain.ChatType
import java.util.concurrent.ConcurrentHashMap

class InMemoryChatsService : ChatsService {
    private val chatsByUserId = ConcurrentHashMap<String, List<ChatSummary>>()

    override fun listChats(userId: String, query: String?, unreadOnly: Boolean): List<ChatSummary> {
        val source = chatsByUserId.computeIfAbsent(userId) { buildSeedChats() }
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()

        return source
            .asSequence()
            .filter { chat ->
                if (normalizedQuery.isBlank()) {
                    true
                } else {
                    chat.title.lowercase().contains(normalizedQuery) ||
                        chat.lastMessagePreview.lowercase().contains(normalizedQuery)
                }
            }
            .filter { chat ->
                if (unreadOnly) chat.unreadCount > 0 else true
            }
            .sortedWith(compareByDescending<ChatSummary> { it.isPinned }.thenByDescending { it.updatedAtEpochMs })
            .toList()
    }

    private fun buildSeedChats(): List<ChatSummary> {
        val now = System.currentTimeMillis()
        return listOf(
            ChatSummary(
                id = "chat-family",
                title = "Family",
                lastMessagePreview = "Do not forget dinner on Saturday.",
                unreadCount = 3,
                isPinned = true,
                updatedAtEpochMs = now - 60_000,
                type = ChatType.GROUP
            ),
            ChatSummary(
                id = "chat-lera",
                title = "Lera",
                lastMessagePreview = "Thanks for your support.",
                unreadCount = 2,
                isPinned = false,
                updatedAtEpochMs = now - 120_000,
                type = ChatType.DIRECT
            ),
            ChatSummary(
                id = "chat-artem",
                title = "Artem",
                lastMessagePreview = "I uploaded files to Space.",
                unreadCount = 1,
                isPinned = false,
                updatedAtEpochMs = now - 300_000,
                type = ChatType.DIRECT
            ),
            ChatSummary(
                id = "chat-work",
                title = "Work group",
                lastMessagePreview = "Great, accepted.",
                unreadCount = 0,
                isPinned = false,
                updatedAtEpochMs = now - 600_000,
                type = ChatType.GROUP
            )
        )
    }
}
