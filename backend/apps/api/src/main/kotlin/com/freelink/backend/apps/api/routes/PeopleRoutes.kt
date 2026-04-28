package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.people.domain.PersonSummary
import com.freelink.backend.libs.people.service.PeopleService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class PersonSummaryDto(
    val id: String,
    val login: String,
    val displayName: String,
    val isOnline: Boolean
)

fun Route.installPeopleRoutes(
    authService: AuthService,
    peopleService: PeopleService
) {
    get("/people") {
        val accessToken = call.request.headers["Authorization"].extractBearerTokenForPeopleRoutes()
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
        val people = peopleService.listPeople(userId, query)
        call.respond(people.map { it.toDto() })
    }
}

private fun PersonSummary.toDto(): PersonSummaryDto {
    return PersonSummaryDto(
        id = id,
        login = login,
        displayName = displayName,
        isOnline = isOnline
    )
}

private fun String?.extractBearerTokenForPeopleRoutes(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}
