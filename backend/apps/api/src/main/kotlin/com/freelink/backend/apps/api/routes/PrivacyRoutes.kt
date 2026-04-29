package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.privacy.domain.PrivacySettings
import com.freelink.backend.libs.privacy.service.PrivacySettingsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable

@Serializable
data class PrivacySettingsDto(
    val hiddenModeEnabled: Boolean,
    val biometricLockRequired: Boolean,
    val disappearingMessagesEnabled: Boolean,
    val linkPreviewEnabled: Boolean,
    val whoCanMessageMe: String
)

fun Route.installPrivacyRoutes(
    authService: AuthService,
    privacySettingsService: PrivacySettingsService
) {
    get("/privacy/settings") {
        val accessToken = call.request.headers["Authorization"].extractBearerTokenForPrivacy()
        if (accessToken.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponseDto(message = "Authorization bearer token is required.")
            )
            return@get
        }

        val userId = authService.resolveUserIdByAccessToken(accessToken)
        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
            return@get
        }

        val settings = privacySettingsService.getSettings(userId)
        call.respond(settings.toDto())
    }

    put("/privacy/settings") {
        val accessToken = call.request.headers["Authorization"].extractBearerTokenForPrivacy()
        if (accessToken.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponseDto(message = "Authorization bearer token is required.")
            )
            return@put
        }

        val userId = authService.resolveUserIdByAccessToken(accessToken)
        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
            return@put
        }

        val request = call.receive<PrivacySettingsDto>()
        val updated = privacySettingsService.updateSettings(userId, request.toDomain())
        call.respond(updated.toDto())
    }
}

private fun PrivacySettings.toDto(): PrivacySettingsDto {
    return PrivacySettingsDto(
        hiddenModeEnabled = hiddenModeEnabled,
        biometricLockRequired = biometricLockRequired,
        disappearingMessagesEnabled = disappearingMessagesEnabled,
        linkPreviewEnabled = linkPreviewEnabled,
        whoCanMessageMe = whoCanMessageMe
    )
}

private fun PrivacySettingsDto.toDomain(): PrivacySettings {
    return PrivacySettings(
        hiddenModeEnabled = hiddenModeEnabled,
        biometricLockRequired = biometricLockRequired,
        disappearingMessagesEnabled = disappearingMessagesEnabled,
        linkPreviewEnabled = linkPreviewEnabled,
        whoCanMessageMe = whoCanMessageMe
    )
}

private fun String?.extractBearerTokenForPrivacy(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}
