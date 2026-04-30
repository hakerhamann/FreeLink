package com.freelink.feature.chat.data

import com.freelink.core.encryption.MessageEnvelopeFactory
import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaAttachment
import com.freelink.core.model.domain.Message
import com.freelink.core.network.auth.AuthApiResult
import com.freelink.core.network.media.MediaApiClient
import com.freelink.core.network.media.mapper.toAttachment
import com.freelink.core.network.messages.MessageApiClient
import com.freelink.core.network.messages.ws.DirectChatWsEvent
import com.freelink.core.network.messages.ws.DirectChatWsEventsClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

sealed interface DirectChatResult<out T> {
    data class Success<T>(val value: T) : DirectChatResult<T>
    data class Failure(val message: String) : DirectChatResult<Nothing>
}

class DirectChatRepository(
    private val authRepository: AuthRepository,
    private val mediaApiClient: MediaApiClient,
    private val messageApiClient: MessageApiClient,
    private val directChatWsEventsClient: DirectChatWsEventsClient,
    private val messageEnvelopeFactory: MessageEnvelopeFactory
) {
    suspend fun currentUserId(): String? {
        return authRepository.currentUserId()
    }

    suspend fun loadMessages(chatId: String): DirectChatResult<List<Message>> {
        return safely {
            when (
                val result = authRepository.authorizedRequest { session ->
                    messageApiClient.fetchMessages(
                        accessToken = session.accessToken,
                        chatId = chatId
                    )
                }
            ) {
                is AuthRepositoryResult.Success -> DirectChatResult.Success(result.value)
                is AuthRepositoryResult.Failure -> DirectChatResult.Failure(result.message)
            }
        }
    }

    suspend fun sendMessage(
        chatId: String,
        body: String,
        attachment: MediaAttachment? = null,
        replyToMessageId: String? = null
    ): DirectChatResult<Message> {
        return safely {
            when (
                val result = authRepository.authorizedRequest { session ->
                    val envelope = messageEnvelopeFactory.create(
                        conversationId = chatId,
                        senderDeviceId = session.deviceId,
                        plaintextBody = body
                    )
                    messageApiClient.sendMessage(
                        accessToken = session.accessToken,
                        chatId = chatId,
                        body = body,
                        attachment = attachment,
                        envelope = envelope,
                        replyToMessageId = replyToMessageId
                    )
                }
            ) {
                is AuthRepositoryResult.Success -> DirectChatResult.Success(result.value)
                is AuthRepositoryResult.Failure -> DirectChatResult.Failure(result.message)
            }
        }
    }

    suspend fun uploadSampleAttachment(chatId: String): DirectChatResult<MediaAttachment> {
        return uploadSampleAttachment(chatId, AttachmentType.PHOTO)
    }

    suspend fun uploadSampleAttachment(
        chatId: String,
        attachmentType: AttachmentType
    ): DirectChatResult<MediaAttachment> {
        return safely {
            when (
                val result = authRepository.authorizedRequest { session ->
                    val preset = sampleAttachmentPreset(chatId, attachmentType)
                    val init = mediaApiClient.initUpload(
                        accessToken = session.accessToken,
                        fileName = preset.fileName,
                        mimeType = preset.mimeType,
                        digestSha256 = preset.digestSha256,
                        byteSize = preset.byteSize,
                        attachmentType = preset.attachmentType
                    )
                    when (init) {
                        is AuthApiResult.Failure -> init
                        is AuthApiResult.Success -> mediaApiClient.completeUpload(
                            accessToken = session.accessToken,
                            uploadId = init.value.uploadId
                        )
                    }
                }
            ) {
                is AuthRepositoryResult.Success -> DirectChatResult.Success(result.value.toAttachment())
                is AuthRepositoryResult.Failure -> DirectChatResult.Failure(result.message)
            }
        }
    }

    suspend fun setReaction(
        chatId: String,
        messageId: String,
        emoji: String
    ): DirectChatResult<Message> {
        return safely {
            when (
                val result = authRepository.authorizedRequest { session ->
                    messageApiClient.setReaction(
                        accessToken = session.accessToken,
                        chatId = chatId,
                        messageId = messageId,
                        emoji = emoji
                    )
                }
            ) {
                is AuthRepositoryResult.Success -> DirectChatResult.Success(result.value)
                is AuthRepositoryResult.Failure -> DirectChatResult.Failure(result.message)
            }
        }
    }

    suspend fun observeRealtimeEvents(
        chatId: String,
        onEvent: suspend (DirectChatWsEvent) -> Unit
    ) {
        var reconnectAttempt = 0
        while (true) {
            val accessToken = authRepository.currentAccessToken() ?: return
            try {
                directChatWsEventsClient.collectEvents(
                    accessToken = accessToken,
                    chatId = chatId,
                    onEvent = onEvent
                )
                reconnectAttempt = 0
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                reconnectAttempt += 1
                val backoffMs = (reconnectAttempt * 1_000L).coerceAtMost(5_000L)
                delay(backoffMs)
            }
        }
    }

    private suspend fun <T> safely(
        block: suspend () -> DirectChatResult<T>
    ): DirectChatResult<T> {
        return try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            DirectChatResult.Failure("Could not complete chat action.")
        }
    }

    private fun sampleAttachmentPreset(
        chatId: String,
        attachmentType: AttachmentType
    ): SampleAttachmentPreset {
        val timestamp = System.currentTimeMillis()
        val baseName = "chat-$chatId-$timestamp"
        return when (attachmentType) {
            AttachmentType.PHOTO -> SampleAttachmentPreset(
                fileName = "photo-$chatId.jpg",
                mimeType = "image/jpeg",
                digestSha256 = "$baseName-photo",
                byteSize = 2_048,
                attachmentType = attachmentType
            )
            AttachmentType.FILE -> SampleAttachmentPreset(
                fileName = "brief-$chatId.pdf",
                mimeType = "application/pdf",
                digestSha256 = "$baseName-file",
                byteSize = 48_512,
                attachmentType = attachmentType
            )
            AttachmentType.VOICE -> SampleAttachmentPreset(
                fileName = "voice-$chatId.m4a",
                mimeType = "audio/mp4",
                digestSha256 = "$baseName-voice",
                byteSize = 8_192,
                attachmentType = attachmentType
            )
            AttachmentType.VIDEO -> SampleAttachmentPreset(
                fileName = "clip-$chatId.mp4",
                mimeType = "video/mp4",
                digestSha256 = "$baseName-video",
                byteSize = 120_000,
                attachmentType = attachmentType
            )
        }
    }

    private data class SampleAttachmentPreset(
        val fileName: String,
        val mimeType: String,
        val digestSha256: String,
        val byteSize: Long,
        val attachmentType: AttachmentType
    )
}
