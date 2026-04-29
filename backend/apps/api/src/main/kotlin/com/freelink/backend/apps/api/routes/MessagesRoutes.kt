package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.messaging.domain.ChatMessage
import com.freelink.backend.libs.messaging.model.EncryptedEnvelope
import com.freelink.backend.libs.messaging.service.MessagingService
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
    val deliveryState: String,
    val envelope: EncryptedEnvelopeDto? = null
)

@Serializable
data class SendMessageRequestDto(
    val chatId: String,
    val body: String,
    val envelope: EncryptedEnvelopeDto? = null
)

fun Route.installMessageRoutes(
    authService: AuthService,
    messagingService: MessagingService
) {
    get("/messages") {
        val accessToken = call.request.headers["Authorization"].extractBearerTokenForMessages()
        if (accessToken.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Authorization bearer token is required."))
            return@get
        }

        val userId = authService.resolveUserIdByAccessToken(accessToken)
        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
            return@get
        }

        val chatId = call.request.queryParameters["chatId"]?.trim()
        if (chatId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "chatId query parameter is required."))
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
        val accessToken = call.request.headers["Authorization"].extractBearerTokenForMessages()
        if (accessToken.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Authorization bearer token is required."))
            return@post
        }

        val userId = authService.resolveUserIdByAccessToken(accessToken)
        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
            return@post
        }

        val request = call.receive<SendMessageRequestDto>()
        if (request.chatId.isBlank() || request.body.trim().length !in 1..4000) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "Invalid message payload."))
            return@post
        }

        val created = messagingService.sendMessage(
            userId = userId,
            chatId = request.chatId.trim(),
            body = request.body.trim(),
            envelope = request.envelope?.toDomain()
        )

        call.respond(HttpStatusCode.Created, created.toDto())
    }
}

private fun ChatMessage.toDto(): MessageDto {
    return MessageDto(
        id = id,
        chatId = chatId,
        senderUserId = senderUserId,
        body = body,
        createdAtEpochMs = createdAtEpochMs,
        deliveryState = deliveryState.name,
        envelope = envelope?.toDto()
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

private fun String?.extractBearerTokenForMessages(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}
