package com.freelink.feature.chat.ui.mapper

import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaAttachment
import com.freelink.core.model.domain.Message
import com.freelink.core.model.domain.MessageDeliveryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DirectChatUiMapperTest {

    @Test
    fun `message mapper keeps attachment metadata for bubble rendering`() {
        val message = Message(
            id = "message-1",
            chatId = "chat-1",
            senderUserId = "user-1",
            body = "",
            createdAtEpochMs = 1_700_000_000_000,
            deliveryState = MessageDeliveryState.SENT,
            attachment = MediaAttachment(
                id = "attachment-1",
                type = AttachmentType.PHOTO,
                fileName = "sunset.jpg",
                digestSha256 = "digest-1",
                byteSize = 2048,
                mimeType = "image/jpeg",
                downloadUrl = "https://example.local/sunset.jpg"
            )
        )

        val mapped = message.toDirectUiModel(
            currentUserId = "user-1",
            replyToSnippet = null
        )

        assertNotNull(mapped.attachment)
        assertEquals("Photo", mapped.attachment?.typeLabel)
        assertEquals("sunset.jpg", mapped.attachment?.fileName)
        assertEquals("https://example.local/sunset.jpg", mapped.attachment?.downloadUrl)
    }
}
