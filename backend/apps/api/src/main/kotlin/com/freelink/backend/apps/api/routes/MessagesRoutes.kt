package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.chats.service.ChatsService
import com.freelink.backend.libs.messaging.domain.ChatMessage
import com.freelink.backend.libs.messaging.domain.MessageAttachment
import com.freelink.backend.libs.messaging.model.EncryptedEnvelope
import com.freelink.backend.libs.messaging.service.MessagingService
import com.freelink.backend.libs.privacy.service.PrivacySettingsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class EncryptedEnvelopeDto(
    val version: Int,
    val conversationId: String,
    val senderDeviceId: String,
    val ciphertext: String,
    val nonce: String,
    val sentAt: String
)

@Serializable
data class MessageDto(
    val id: String,
    val chatId: String,
    val senderUserId: String,
    val body: String,
    val createdAtEpochMs: Long,
    val expiresAtEpochMs: Long? = null,
    val deliveryState: String,
    val attachment: MessageAttachmentDto? = null,
    val envelope: EncryptedEnvelopeDto? = null,
    val replyToMessageId: String? = null,
    val reactions: Map<String, Int> = emptyMap()
)

@Serializable
data class MessageAttachmentDto(
    val id: String,
    val type: String,
    val fileName: String,
    val digestSha256: String,
    val byteSize: Long,
    val mimeType: String,
    val downloadUrl: String
)

@Serializable
data class SendMessageRequestDto(
    val chatId: String,
    val body: String,
    val attachment: MessageAttachmentDto? = null,
    val envelope: EncryptedEnvelopeDto? = null,
    val replyToMessageId: String? = null
)

@Serializable
data class SetReactionRequestDto(
    val chatId: String,
    val emoji: String
)

fun Route.installMessageRoutes(
    authService: AuthService,
    messagingService: MessagingService,
    privacySettingsService: PrivacySettingsService,
    chatsService: ChatsService
) {
    get("/messages") {
        val userId = requireAuthorizedUserId(call, authService) ?: return@get

        val chatId = call.request.queryParameters["chatId"]?.trim()
        if (chatId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "chatId query parameter is required."))
            return@get
        }
        if (!canUseChat(userId, chatId, chatsService, privacySettingsService)) {
            call.respond(HttpStatusCode.Forbidden, ErrorResponseDto(message = trustedChatPolicyError))
            return@get
        }

        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
        val messages = messagingService.listMessages(
            userId = userId,
            chatId = chatId,
            limit = limit
        )

        call.respond(messages.map { it.toDto() })
    }

    post("/messages") {
        val userId = requireAuthorizedUserId(call, authService) ?: return@post

        val request = call.receive<SendMessageRequestDto>()
        val trimmedBody = request.body.trim()
        val hasBody = trimmedBody.isNotBlank()
        val hasAttachment = request.attachment != null
        val bodyLengthValid = trimmedBody.length <= 4000
        if (request.chatId.isBlank() || !bodyLengthValid || (!hasBody && !hasAttachment)) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "Invalid message payload."))
            return@post
        }
        val chatId = request.chatId.trim()
        if (!canUseChat(userId, chatId, chatsService, privacySettingsService)) {
            call.respond(HttpStatusCode.Forbidden, ErrorResponseDto(message = trustedChatPolicyError))
            return@post
        }

        val created = messagingService.sendMessage(
            userId = userId,
            chatId = chatId,
            body = trimmedBody,
            attachment = request.attachment?.toDomain(),
            envelope = request.envelope?.toDomain(),
            replyToMessageId = request.replyToMessageId?.trim().takeUnless { it.isNullOrBlank() },
            expiresAtEpochMs = privacySettingsService.messageExpiresAtEpochMs(userId)
        )

        call.respond(HttpStatusCode.Created, created.toDto())
    }

    post("/messages/{messageId}/reactions") {
        val userId = requireAuthorizedUserId(call, authService) ?: return@post
        val messageId = call.parameters["messageId"]?.trim()
        if (messageId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "messageId is required."))
            return@post
        }

        val request = call.receive<SetReactionRequestDto>()
        val emoji = request.emoji.trim()
        val chatId = request.chatId.trim()
        if (chatId.isBlank() || emoji.length !in 1..16) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "Invalid reaction payload."))
            return@post
        }
        if (!canUseChat(userId, chatId, chatsService, privacySettingsService)) {
            call.respond(HttpStatusCode.Forbidden, ErrorResponseDto(message = trustedChatPolicyError))
            return@post
        }

        val updated = messagingService.setReaction(
            userId = userId,
            chatId = chatId,
            messageId = messageId,
            emoji = emoji
        )
        if (updated == null) {
            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(message = "Message not found."))
            return@post
        }

        call.respond(HttpStatusCode.OK, updated.toDto())
    }
}

private fun canUseChat(
    userId: String,
    chatId: String,
    chatsService: ChatsService,
    privacySettingsService: PrivacySettingsService
): Boolean {
    val settings = privacySettingsService.getSettings(userId)
    return settings.whoCanMessageMe == "everyone" || chatsService.isKnownChat(userId, chatId)
}

private const val trustedChatPolicyError = "Chat is outside trusted messaging policy."

private suspend fun requireAuthorizedUserId(
    call: io.ktor.server.application.ApplicationCall,
    authService: AuthService
): String? {
    val accessToken = call.request.headers["Authorization"].extractBearerTokenForMessages()
    if (accessToken.isNullOrBlank()) {
        call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Authorization bearer token is required."))
        return null
    }

    val userId = authService.resolveUserIdByAccessToken(accessToken)
    if (userId.isNullOrBlank()) {
        call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
        return null
    }
    return userId
}

private fun ChatMessage.toDto(): MessageDto {
    return MessageDto(
        id = id,
        chatId = chatId,
        senderUserId = senderUserId,
        body = body,
        createdAtEpochMs = createdAtEpochMs,
        expiresAtEpochMs = expiresAtEpochMs,
        deliveryState = deliveryState.name,
        attachment = attachment?.toDto(),
        envelope = envelope?.toDto(),
        replyToMessageId = replyToMessageId,
        reactions = reactions
    )
}

private fun MessageAttachmentDto.toDomain(): MessageAttachment {
    return MessageAttachment(
        id = id,
        type = type,
        fileName = fileName,
        digestSha256 = digestSha256,
        byteSize = byteSize,
        mimeType = mimeType,
        downloadUrl = downloadUrl
    )
}

private fun MessageAttachment.toDto(): MessageAttachmentDto {
    return MessageAttachmentDto(
        id = id,
        type = type,
        fileName = fileName,
        digestSha256 = digestSha256,
        byteSize = byteSize,
        mimeType = mimeType,
        downloadUrl = downloadUrl
    )
}

private fun EncryptedEnvelopeDto.toDomain(): EncryptedEnvelope {
    return EncryptedEnvelope(
        version = version,
        conversationId = conversationId,
        senderDeviceId = senderDeviceId,
        ciphertext = ciphertext,
        nonce = nonce,
        sentAt = sentAt
    )
}

private fun EncryptedEnvelope.toDto(): EncryptedEnvelopeDto {
    return EncryptedEnvelopeDto(
        version = version,
        conversationId = conversationId,
        senderDeviceId = senderDeviceId,
        ciphertext = ciphertext,
        nonce = nonce,
        sentAt = sentAt
    )
}

private fun PrivacySettingsService.messageExpiresAtEpochMs(userId: String): Long? {
    val settings = getSettings(userId)
    return if (settings.disappearingMessagesEnabled) {
        System.currentTimeMillis() + disappearingMessageTtlMs
    } else {
        null
    }
}

private const val disappearingMessageTtlMs = 24L * 60L * 60L * 1000L

private fun String?.extractBearerTokenForMessages(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}
