package com.freelink.backend.libs.chats.service

import com.freelink.backend.libs.chats.domain.ChatSummary
import com.freelink.backend.libs.chats.domain.ChatType
import java.time.Instant

class InMemoryChatsService : ChatsService {
    private val chatsByUser = mutableMapOf<String, MutableList<ChatSummary>>()
    private val archivedByUser = mutableMapOf<String, MutableList<ChatSummary>>()

    override fun listChats(userId: String, query: String?, unreadOnly: Boolean): List<ChatSummary> {
        val chats = chatsByUser.getOrPut(userId) { seedChats().toMutableList() }
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        return chats
            .asSequence()
            .filter { chat -> !unreadOnly || chat.unreadCount > 0 }
            .filter { chat ->
                normalizedQuery.isBlank() ||
                    chat.title.lowercase().contains(normalizedQuery) ||
                    chat.lastMessagePreview.lowercase().contains(normalizedQuery)
            }
            .sortedWith(compareByDescending<ChatSummary> { it.isPinned }.thenByDescending { it.updatedAtEpochMs })
            .toList()
    }


    override fun isKnownChat(userId: String, chatId: String): Boolean {
        return chatsByUser.getOrPut(userId) { seedChats().toMutableList() }.any { it.id == chatId } ||
            archivedByUser[userId].orEmpty().any { it.id == chatId }
    }

    override fun archiveChat(userId: String, chatId: String): Boolean {
        val chats = chatsByUser.getOrPut(userId) { seedChats().toMutableList() }
        val chat = chats.firstOrNull { it.id == chatId } ?: return false
        chats.remove(chat)
        archivedByUser.getOrPut(userId) { mutableListOf() }.add(chat.copy(isPinned = false))
        return true
    }

    override fun listArchivedChats(userId: String): List<ChatSummary> {
        return archivedByUser[userId].orEmpty().sortedByDescending { it.updatedAtEpochMs }
    }

    override fun restoreArchivedChat(userId: String, chatId: String): Boolean {
        val archived = archivedByUser[userId] ?: return false
        val chat = archived.firstOrNull { it.id == chatId } ?: return false
        archived.remove(chat)
        chatsByUser.getOrPut(userId) { seedChats().toMutableList() }.add(chat)
        return true
    }

    fun upsertLastMessage(userId: String, chatId: String, preview: String, at: Long = Instant.now().toEpochMilli()) {
        val chats = chatsByUser.getOrPut(userId) { seedChats().toMutableList() }
        val index = chats.indexOfFirst { it.id == chatId }
        if (index >= 0) {
            chats[index] = chats[index].copy(lastMessagePreview = preview, updatedAtEpochMs = at)
        }
    }

    private fun seedChats(now: Long = Instant.now().toEpochMilli()): List<ChatSummary> {
        return listOf(
            ChatSummary(
                id = "chat-family",
                title = "Семья",
                lastMessagePreview = "Не забудьте, в субботу ужин у нас дома.",
                unreadCount = 3,
                isPinned = true,
                updatedAtEpochMs = now - 300_000,
                type = ChatType.GROUP
            ),
            ChatSummary(
                id = "chat-lera",
                title = "Лера",
                lastMessagePreview = "Спасибо за поддержку, ты лучшая!",
                unreadCount = 2,
                isPinned = false,
                updatedAtEpochMs = now - 360_000,
                type = ChatType.DIRECT
            ),
            ChatSummary(
                id = "chat-artem",
                title = "Артём",
                lastMessagePreview = "Скинул файлы в пространство.",
                unreadCount = 1,
                isPinned = false,
                updatedAtEpochMs = now - 540_000,
                type = ChatType.DIRECT
            ),
            ChatSummary(
                id = "chat-work",
                title = "Рабочая группа",
                lastMessagePreview = "Отлично, принято.",
                unreadCount = 0,
                isPinned = false,
                updatedAtEpochMs = now - 840_000,
                type = ChatType.GROUP
            )
        )
    }
}
