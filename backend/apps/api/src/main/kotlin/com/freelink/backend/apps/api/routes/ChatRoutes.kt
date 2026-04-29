package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.chats.domain.ChatSummary
import com.freelink.backend.libs.chats.service.ChatsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class ChatSummaryDto(
    val id: String,
    val title: String,
    val lastMessagePreview: String,
    val unreadCount: Int,
    val isPinned: Boolean,
    val updatedAtEpochMs: Long,
    val type: String
)

fun Route.installChatRoutes(
    authService: AuthService,
    chatsService: ChatsService
) {
    get("/chats") {
        val userId = call.requireAuthorizedUserIdForChatRoutes(authService) ?: return@get

        val query = call.request.queryParameters["q"]
        val unreadOnly = call.request.queryParameters["unreadOnly"]?.toBooleanStrictOrNull() ?: false
        val chats = chatsService.listChats(
            userId = userId,
            query = query,
            unreadOnly = unreadOnly
        )

        call.respond(chats.map { it.toDto() })
    }

    get("/archive") {
        val userId = call.requireAuthorizedUserIdForChatRoutes(authService) ?: return@get

        val chats = chatsService.listArchivedChats(userId)
        call.respond(chats.map { it.toDto() })
    }

    post("/archive/{chatId}") {
        val userId = call.requireAuthorizedUserIdForChatRoutes(authService) ?: return@post
        val chatId = call.parameters["chatId"]?.trim()
        if (chatId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "chatId is required."))
            return@post
        }

        val archived = chatsService.archiveChat(
            userId = userId,
            chatId = chatId
        )
        if (!archived) {
            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(message = "Chat not found."))
            return@post
        }

        call.respond(HttpStatusCode.NoContent)
    }

    delete("/archive/{chatId}") {
        val userId = call.requireAuthorizedUserIdForChatRoutes(authService) ?: return@delete
        val chatId = call.parameters["chatId"]?.trim()
        if (chatId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "chatId is required."))
            return@delete
        }

        val restored = chatsService.restoreArchivedChat(
            userId = userId,
            chatId = chatId
        )
        if (!restored) {
            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(message = "Archived chat not found."))
            return@delete
        }

        call.respond(HttpStatusCode.NoContent)
    }
}

private fun ChatSummary.toDto(): ChatSummaryDto {
    return ChatSummaryDto(
        id = id,
        title = title,
        lastMessagePreview = lastMessagePreview,
        unreadCount = unreadCount,
        isPinned = isPinned,
        updatedAtEpochMs = updatedAtEpochMs,
        type = type.name
    )
}

private fun String?.extractBearerTokenForChatRoutes(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}

private suspend fun ApplicationCall.requireAuthorizedUserIdForChatRoutes(
    authService: AuthService
): String? {
    val accessToken = request.headers["Authorization"].extractBearerTokenForChatRoutes()
    if (accessToken.isNullOrBlank()) {
        respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Authorization bearer token is required."))
        return null
    }

    val userId = authService.resolveUserIdByAccessToken(accessToken)
    if (userId.isNullOrBlank()) {
        respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
        return null
    }
    return userId
}
