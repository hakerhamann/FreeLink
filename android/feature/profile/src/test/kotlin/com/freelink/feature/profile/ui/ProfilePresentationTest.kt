package com.freelink.feature.profile.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfilePresentationTest {

    @Test
    fun `buildProfileSections uses user suffix in title`() {
        val sections = buildProfileSections("user-12345678")

        assertEquals("User 12345678", sections.title)
    }

    @Test
    fun `buildProfileSections exposes seeded profile sections`() {
        val sections = buildProfileSections(null)

        assertTrue(sections.sharedChats.isNotEmpty())
        assertTrue(sections.sharedPhotos.isNotEmpty())
        assertTrue(sections.pinnedMessages.isNotEmpty())
        assertTrue(sections.trustedContacts.isNotEmpty())
    }
}
