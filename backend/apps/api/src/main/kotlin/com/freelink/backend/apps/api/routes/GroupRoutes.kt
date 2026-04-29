package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.groups.domain.GroupSummary
import com.freelink.backend.libs.groups.service.CreateGroupCommand
import com.freelink.backend.libs.groups.service.GroupsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class GroupSummaryDto(
    val id: String,
    val title: String,
    val membersCount: Int,
    val myRole: String,
    val lastMessagePreview: String,
    val updatedAtEpochMs: Long
)

@Serializable
data class CreateGroupRequestDto(
    val title: String,
    val memberUserIds: List<String> = emptyList()
)

fun Route.installGroupRoutes(
    authService: AuthService,
    groupsService: GroupsService
) {
    get("/groups") {
        val accessToken = call.request.headers["Authorization"].extractBearerTokenForGroups()
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
        val groups = groupsService.listGroups(userId, query)
        call.respond(groups.map { it.toDto() })
    }

    post("/groups") {
        val accessToken = call.request.headers["Authorization"].extractBearerTokenForGroups()
        if (accessToken.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Authorization bearer token is required."))
            return@post
        }

        val userId = authService.resolveUserIdByAccessToken(accessToken)
        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
            return@post
        }

        val request = call.receive<CreateGroupRequestDto>()
        val title = request.title.trim()
        if (title.length !in 2..80) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "Invalid group payload."))
            return@post
        }

        val created = groupsService.createGroup(
            ownerUserId = userId,
            command = CreateGroupCommand(
                title = title,
                memberUserIds = request.memberUserIds
            )
        )
        call.respond(HttpStatusCode.Created, created.toDto())
    }
}

private fun GroupSummary.toDto(): GroupSummaryDto {
    return GroupSummaryDto(
        id = id,
        title = title,
        membersCount = membersCount,
        myRole = myRole.name,
        lastMessagePreview = lastMessagePreview,
        updatedAtEpochMs = updatedAtEpochMs
    )
}

private fun String?.extractBearerTokenForGroups(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}
