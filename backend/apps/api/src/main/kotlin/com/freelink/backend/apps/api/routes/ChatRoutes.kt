package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.chats.domain.ChatSummary
import com.freelink.backend.libs.chats.service.ChatsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
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
        val accessToken = call.request.headers["Authorization"].extractBearerTokenForChatRoutes()
        if (accessToken.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Authorization bearer token is required."))
            return@get
        }

        val userId = authService.resolveUserIdByAccessToken(accessToken)
        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
            return@get
        }

        val query = call.request.queryParameters["q"]
        val unreadOnly = call.request.queryParameters["unreadOnly"]?.toBooleanStrictOrNull() ?: false
        val chats = chatsService.listChats(
            userId = userId,
            query = query,
            unreadOnly = unreadOnly
        )

        call.respond(chats.map { it.toDto() })
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
