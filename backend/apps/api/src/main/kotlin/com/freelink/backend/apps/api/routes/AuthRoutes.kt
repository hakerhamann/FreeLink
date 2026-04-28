package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.domain.DeviceSession
import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.auth.service.LoginCommand
import com.freelink.backend.libs.auth.service.RegisterCommand
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val login: String,
    val password: String,
    val deviceName: String = "Android Device"
)

@Serializable
data class LoginRequestDto(
    val login: String,
    val password: String,
    val deviceName: String = "Android Device"
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String
)

@Serializable
data class LogoutRequestDto(
    val refreshToken: String
)

@Serializable
data class AuthResponseDto(
    val userId: String,
    val deviceId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long
)

@Serializable
data class DeviceSessionDto(
    val deviceId: String,
    val deviceName: String,
    val lastSeenAtIso: String,
    val isCurrent: Boolean
)

@Serializable
data class ErrorResponseDto(
    val message: String
)

fun Route.installAuthRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequestDto>()
            val result = authService.register(
                RegisterCommand(
                    login = request.login,
                    password = request.password,
                    deviceName = request.deviceName
                )
            )

            if (result == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "Invalid registration request."))
                return@post
            }

            call.respond(
                HttpStatusCode.Created,
                AuthResponseDto(
                    userId = result.userId,
                    deviceId = result.deviceId,
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    expiresInSeconds = result.expiresInSeconds
                )
            )
        }

        post("/login") {
            val request = call.receive<LoginRequestDto>()
            val result = authService.login(
                LoginCommand(
                    login = request.login,
                    password = request.password,
                    deviceName = request.deviceName
                )
            )

            if (result == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponseDto(message = "Invalid credentials or session state.")
                )
                return@post
            }

            call.respond(
                HttpStatusCode.OK,
                AuthResponseDto(
                    userId = result.userId,
                    deviceId = result.deviceId,
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    expiresInSeconds = result.expiresInSeconds
                )
            )
        }

        post("/refresh") {
            val request = call.receive<RefreshRequestDto>()
            val result = authService.refresh(request.refreshToken)

            if (result == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
                return@post
            }

            call.respond(
                HttpStatusCode.OK,
                AuthResponseDto(
                    userId = result.userId,
                    deviceId = result.deviceId,
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    expiresInSeconds = result.expiresInSeconds
                )
            )
        }

        post("/logout") {
            val request = call.receive<LogoutRequestDto>()
            authService.logout(request.refreshToken)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    get("/devices") {
        val accessToken = call.request.headers["Authorization"].extractBearerToken()
        if (accessToken.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Authorization bearer token is required."))
            return@get
        }

        val devices = authService.listDevicesByAccessToken(accessToken)
        if (devices == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto(message = "Invalid credentials or session state."))
            return@get
        }

        call.respond(devices.map(::toDeviceSessionDto))
    }

    delete("/devices/{deviceId}") {
        val accessToken = call.request.headers["Authorization"].extractBearerToken()
        val deviceId = call.parameters["deviceId"]
        if (accessToken.isNullOrBlank() || deviceId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "Missing required request data."))
            return@delete
        }

        val revoked = authService.revokeDeviceByAccessToken(accessToken, deviceId)
        if (!revoked) {
            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(message = "Device not found."))
            return@delete
        }

        call.respond(HttpStatusCode.NoContent)
    }
}

private fun String?.extractBearerToken(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}

private fun toDeviceSessionDto(session: DeviceSession): DeviceSessionDto {
    return DeviceSessionDto(
        deviceId = session.deviceId,
        deviceName = session.deviceName,
        lastSeenAtIso = session.lastSeenAtIso,
        isCurrent = session.isCurrent
    )
}
