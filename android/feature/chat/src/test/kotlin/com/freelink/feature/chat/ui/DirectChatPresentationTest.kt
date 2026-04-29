package com.freelink.feature.chat.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DirectChatPresentationTest {

    @Test
    fun `defaultAttachmentTrayActions keeps tray order stable`() {
        val actions = defaultAttachmentTrayActions()

        assertEquals(listOf("photo", "video", "file", "voice"), actions.map { it.id })
        assertEquals(listOf("Photo", "Video", "File", "Voice"), actions.map { it.label })
    }

    @Test
    fun `attachmentTypeForAction maps supported tray ids`() {
        assertEquals("PHOTO", attachmentTypeForAction("photo")?.name)
        assertEquals("VIDEO", attachmentTypeForAction("video")?.name)
        assertEquals("FILE", attachmentTypeForAction("file")?.name)
        assertEquals("VOICE", attachmentTypeForAction("voice")?.name)
        assertNull(attachmentTypeForAction("unknown"))
    }
}
