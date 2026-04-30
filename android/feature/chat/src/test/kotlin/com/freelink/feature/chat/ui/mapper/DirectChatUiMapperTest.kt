package com.freelink.feature.chat.ui.mapper

import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaAttachment
import com.freelink.core.model.domain.Message
import com.freelink.core.model.domain.MessageDeliveryState
import com.freelink.feature.chat.ui.model.DirectMessageAttachmentKindUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        assertEquals("\u0424\u043e\u0442\u043e", mapped.attachment?.typeLabel)
        assertEquals("sunset.jpg", mapped.attachment?.fileName)
        assertEquals("Предпросмотр фото", mapped.attachment?.previewLabel)
        assertEquals("https://example.local/sunset.jpg", mapped.attachment?.downloadUrl)
    }

    @Test
    fun `voice attachment maps waveform stub metadata`() {
        val message = Message(
            id = "message-voice",
            chatId = "chat-1",
            senderUserId = "user-1",
            body = "",
            createdAtEpochMs = 1_700_000_000_000,
            deliveryState = MessageDeliveryState.SENT,
            attachment = MediaAttachment(
                id = "attachment-voice",
                type = AttachmentType.VOICE,
                fileName = "note.m4a",
                digestSha256 = "digest-voice",
                byteSize = 8_192,
                mimeType = "audio/mp4",
                downloadUrl = "https://example.local/note.m4a"
            )
        )

        val mapped = message.toDirectUiModel(
            currentUserId = "user-1",
            replyToSnippet = null
        )

        assertEquals(DirectMessageAttachmentKindUiModel.VOICE, mapped.attachment?.kind)
        assertEquals("Голосовое сообщение", mapped.attachment?.previewLabel)
        assertEquals("00:16", mapped.attachment?.durationLabel)
        assertFalse(mapped.attachment?.waveformBars.isNullOrEmpty())
    }

    @Test
    fun `file attachment maps file preview label`() {
        val message = Message(
            id = "message-file",
            chatId = "chat-1",
            senderUserId = "user-1",
            body = "",
            createdAtEpochMs = 1_700_000_000_000,
            deliveryState = MessageDeliveryState.SENT,
            attachment = MediaAttachment(
                id = "attachment-file",
                type = AttachmentType.FILE,
                fileName = "brief.pdf",
                digestSha256 = "digest-file",
                byteSize = 48_512,
                mimeType = "application/pdf",
                downloadUrl = "https://example.local/brief.pdf"
            )
        )

        val mapped = message.toDirectUiModel(
            currentUserId = "user-1",
            replyToSnippet = null
        )

        assertEquals(DirectMessageAttachmentKindUiModel.FILE, mapped.attachment?.kind)
        assertEquals("Файл", mapped.attachment?.previewLabel)
        assertEquals("47.4 KB", mapped.attachment?.sizeLabel)
    }

    @Test
    fun `message mapper exposes disappearing message expiry label`() {
        val message = Message(
            id = "message-expiring",
            chatId = "chat-1",
            senderUserId = "user-1",
            body = "temporary",
            createdAtEpochMs = 1_700_000_000_000,
            expiresAtEpochMs = 1_700_003_600_000,
            deliveryState = MessageDeliveryState.SENT
        )

        val mapped = message.toDirectUiModel(
            currentUserId = "user-1",
            replyToSnippet = null
        )

        assertNotNull(mapped.expiresAtLabel)
        assertTrue(mapped.expiresAtLabel?.startsWith("Исчезнет в ") == true)
    }

    @Test
    fun `message mapper extracts first link preview url`() {
        val message = Message(
            id = "message-link",
            chatId = "chat-1",
            senderUserId = "user-1",
            body = "Look https://freelink.local/privacy when you can",
            createdAtEpochMs = 1_700_000_000_000,
            deliveryState = MessageDeliveryState.SENT
        )

        val mapped = message.toDirectUiModel(
            currentUserId = "user-1",
            replyToSnippet = null
        )

        assertEquals("https://freelink.local/privacy", mapped.linkPreviewUrl)
    }
}
