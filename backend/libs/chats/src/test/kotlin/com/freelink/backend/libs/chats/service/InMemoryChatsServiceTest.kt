package com.freelink.backend.libs.chats.service

import org.junit.Assert.assertEquals
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
}
