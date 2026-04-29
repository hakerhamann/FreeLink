package com.freelink.backend.libs.media.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryMediaServiceTest {

    @Test
    fun `init then complete stores media`() {
        val service = InMemoryMediaService()

        val session = service.initUpload(
            userId = "user-1",
            command = MediaInitCommand(
                fileName = "lake.jpg",
                mimeType = "image/jpeg",
                digestSha256 = "digest-1",
                byteSize = 1024,
                attachmentType = "PHOTO"
            )
        )
        val completed = service.completeUpload("user-1", session.uploadId)

        assertFalse(session.alreadyExists)
        assertNotNull(completed)
        assertEquals("lake.jpg", completed!!.fileName)
    }

    @Test
    fun `duplicate digest returns already exists session`() {
        val service = InMemoryMediaService()

        val first = service.initUpload(
            userId = "user-1",
            command = MediaInitCommand(
                fileName = "lake.jpg",
                mimeType = "image/jpeg",
                digestSha256 = "digest-dup",
                byteSize = 1024,
                attachmentType = "PHOTO"
            )
        )
        val firstMedia = service.completeUpload("user-1", first.uploadId)
        val second = service.initUpload(
            userId = "user-1",
            command = MediaInitCommand(
                fileName = "lake-copy.jpg",
                mimeType = "image/jpeg",
                digestSha256 = "digest-dup",
                byteSize = 1024,
                attachmentType = "PHOTO"
            )
        )

        assertNotNull(firstMedia)
        assertTrue(second.alreadyExists)
        assertEquals(firstMedia!!.attachmentId, second.attachmentId)
    }
}
