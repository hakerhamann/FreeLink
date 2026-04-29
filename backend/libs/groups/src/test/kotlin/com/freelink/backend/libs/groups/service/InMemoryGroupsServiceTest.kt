package com.freelink.backend.libs.groups.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryGroupsServiceTest {

    @Test
    fun `listGroups returns seeded groups for known user`() {
        val service = InMemoryGroupsService()

        val groups = service.listGroups(userId = "user-demo", query = null)

        assertEquals(2, groups.size)
        assertTrue(groups.all { it.membersCount >= 1 })
    }

    @Test
    fun `createGroup attaches owner and distinct members`() {
        val service = InMemoryGroupsService()

        val created = service.createGroup(
            ownerUserId = "user-demo",
            command = CreateGroupCommand(
                title = "New Group",
                memberUserIds = listOf("user-a", "user-a", " ", "user-demo", "user-b")
            )
        )

        assertEquals("New Group", created.title)
        assertEquals(3, created.membersCount)
        assertEquals("OWNER", created.myRole.name)
    }

    @Test
    fun `getGroupDetails returns members for seeded user`() {
        val service = InMemoryGroupsService()

        val listed = service.listGroups(userId = "user-test", query = null)
        val details = service.getGroupDetails(
            userId = "user-test",
            groupId = listed.first().id
        )

        assertTrue(details != null)
        assertTrue(details!!.members.isNotEmpty())
        assertEquals("You", details.members.first().displayName)
    }
}
