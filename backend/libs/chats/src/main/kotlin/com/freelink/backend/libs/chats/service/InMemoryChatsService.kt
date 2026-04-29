package com.freelink.backend.libs.chats.service

import com.freelink.backend.libs.chats.domain.ChatSummary
import com.freelink.backend.libs.chats.domain.ChatType
import java.util.concurrent.ConcurrentHashMap

class InMemoryChatsService : ChatsService {
    private val chatsByUserId = ConcurrentHashMap<String, List<ChatSummary>>()
    private val archivedChatIdsByUserId = ConcurrentHashMap<String, MutableSet<String>>()

    override fun listChats(userId: String, query: String?, unreadOnly: Boolean): List<ChatSummary> {
        val source = chatsByUserId.computeIfAbsent(userId) { buildSeedChats() }
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        val archivedChatIds = archivedChatIdsByUserId[userId].orEmpty()

        return source
            .asSequence()
            .filterNot { chat -> chat.id in archivedChatIds }
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

    override fun archiveChat(userId: String, chatId: String): Boolean {
        val source = chatsByUserId.computeIfAbsent(userId) { buildSeedChats() }
        val normalizedChatId = chatId.trim()
        if (source.none { it.id == normalizedChatId }) {
            return false
        }

        val archivedChatIds = archivedChatIdsByUserId.computeIfAbsent(userId) { linkedSetOf() }
        archivedChatIds.add(normalizedChatId)
        return true
    }

    override fun restoreArchivedChat(userId: String, chatId: String): Boolean {
        val source = chatsByUserId.computeIfAbsent(userId) { buildSeedChats() }
        val normalizedChatId = chatId.trim()
        if (source.none { it.id == normalizedChatId }) {
            return false
        }

        val archivedChatIds = archivedChatIdsByUserId[userId] ?: return false
        return archivedChatIds.remove(normalizedChatId)
    }

    override fun listArchivedChats(userId: String): List<ChatSummary> {
        val source = chatsByUserId.computeIfAbsent(userId) { buildSeedChats() }
        val archivedChatIds = archivedChatIdsByUserId[userId].orEmpty()

        return source
            .asSequence()
            .filter { chat -> chat.id in archivedChatIds }
            .sortedByDescending { it.updatedAtEpochMs }
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
