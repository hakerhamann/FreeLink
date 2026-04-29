package com.freelink.backend.libs.chats.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryChatsServiceTest {

    @Test
    fun listChatsReturnsPinnedFirstAndSortedByUpdatedAt() {
        val service = InMemoryChatsService()

        val chats = service.listChats(
            userId = "user-1",
            query = null,
            unreadOnly = false
        )

        assertTrue(chats.isNotEmpty())
        assertTrue(chats.first().isPinned)
    }

    @Test
    fun listChatsSupportsQueryAndUnreadFilter() {
        val service = InMemoryChatsService()

        val queried = service.listChats(
            userId = "user-1",
            query = "files",
            unreadOnly = false
        )
        assertEquals(1, queried.size)
        assertEquals("chat-artem", queried.first().id)

        val unreadOnly = service.listChats(
            userId = "user-1",
            query = null,
            unreadOnly = true
        )
        assertTrue(unreadOnly.all { it.unreadCount > 0 })
    }

    @Test
    fun archiveChatRemovesChatFromDefaultList() {
        val service = InMemoryChatsService()

        assertTrue(service.archiveChat(userId = "user-1", chatId = "chat-lera"))

        val chats = service.listChats(
            userId = "user-1",
            query = null,
            unreadOnly = false
        )

        assertTrue(chats.none { it.id == "chat-lera" })
    }

    @Test
    fun isKnownChatMatchesSeededTrustedSurface() {
        val service = InMemoryChatsService()

        assertTrue(service.isKnownChat(userId = "user-1", chatId = "chat-lera"))
        assertFalse(service.isKnownChat(userId = "user-1", chatId = "chat-unknown"))
    }

    @Test
    fun listArchivedChatsReturnsOnlyArchivedItems() {
        val service = InMemoryChatsService()

        assertTrue(service.archiveChat(userId = "user-1", chatId = "chat-lera"))
        assertTrue(service.archiveChat(userId = "user-1", chatId = "chat-artem"))

        val archived = service.listArchivedChats(userId = "user-1")

        assertEquals(listOf("chat-lera", "chat-artem"), archived.map { it.id })
    }

    @Test
    fun restoreArchivedChatReturnsChatToDefaultList() {
        val service = InMemoryChatsService()

        assertTrue(service.archiveChat(userId = "user-1", chatId = "chat-lera"))
        assertTrue(service.restoreArchivedChat(userId = "user-1", chatId = "chat-lera"))

        val archived = service.listArchivedChats(userId = "user-1")
        val chats = service.listChats(userId = "user-1", query = null, unreadOnly = false)

        assertTrue(archived.none { it.id == "chat-lera" })
        assertTrue(chats.any { it.id == "chat-lera" })
    }
}
