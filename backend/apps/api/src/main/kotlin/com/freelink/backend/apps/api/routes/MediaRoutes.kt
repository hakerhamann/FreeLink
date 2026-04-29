package com.freelink.backend.apps.api.routes

import com.freelink.backend.libs.auth.service.AuthService
import com.freelink.backend.libs.media.domain.MediaUploadSession
import com.freelink.backend.libs.media.domain.StoredMedia
import com.freelink.backend.libs.media.service.MediaInitCommand
import com.freelink.backend.libs.media.service.MediaService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class MediaInitRequestDto(
    val fileName: String,
    val mimeType: String,
    val digestSha256: String,
    val byteSize: Long,
    val attachmentType: String
)

@Serializable
data class MediaCompleteRequestDto(
    val uploadId: String
)

@Serializable
data class MediaUploadSessionDto(
    val uploadId: String,
    val attachmentId: String? = null,
    val blobKey: String,
    val uploadUrl: String,
    val alreadyExists: Boolean
)

@Serializable
data class StoredMediaDto(
    val attachmentId: String,
    val digestSha256: String,
    val byteSize: Long,
    val mimeType: String,
    val fileName: String,
    val attachmentType: String,
    val blobKey: String,
    val downloadUrl: String
)

fun Route.installMediaRoutes(
    authService: AuthService,
    mediaService: MediaService
) {
    post("/media/init") {
        val userId = requireAuthorizedUserIdForMedia(call, authService) ?: return@post
        val request = call.receive<MediaInitRequestDto>()
        if (
            request.fileName.isBlank() ||
            request.mimeType.isBlank() ||
            request.digestSha256.isBlank() ||
            request.byteSize <= 0 ||
            request.attachmentType.isBlank()
        ) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "Invalid media init payload."))
            return@post
        }

        val session = mediaService.initUpload(
            userId = userId,
            command = MediaInitCommand(
                fileName = request.fileName.trim(),
                mimeType = request.mimeType.trim(),
                digestSha256 = request.digestSha256.trim(),
                byteSize = request.byteSize,
                attachmentType = request.attachmentType.trim()
            )
        )
        call.respond(HttpStatusCode.OK, session.toDto())
    }

    post("/media/complete") {
        val userId = requireAuthorizedUserIdForMedia(call, authService) ?: return@post
        val request = call.receive<MediaCompleteRequestDto>()
        val uploadId = request.uploadId.trim()
        if (uploadId.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = "uploadId is required."))
            return@post
        }

        val stored = mediaService.completeUpload(userId, uploadId)
        if (stored == null) {
            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(message = "Upload session not found."))
            return@post
        }

        call.respond(HttpStatusCode.OK, stored.toDto())
    }
}

private fun MediaUploadSession.toDto(): MediaUploadSessionDto {
    return MediaUploadSessionDto(
        uploadId = uploadId,
        attachmentId = attachmentId,
        blobKey = blobKey,
        uploadUrl = uploadUrl,
        alreadyExists = alreadyExists
    )
}

private fun StoredMedia.toDto(): StoredMediaDto {
    return StoredMediaDto(
        attachmentId = attachmentId,
        digestSha256 = digestSha256,
        byteSize = byteSize,
        mimeType = mimeType,
        fileName = fileName,
        attachmentType = attachmentType,
        blobKey = blobKey,
        downloadUrl = downloadUrl
    )
}

private suspend fun requireAuthorizedUserIdForMedia(
    call: io.ktor.server.application.ApplicationCall,
    authService: AuthService
): String? {
    val accessToken = call.request.headers["Authorization"].extractBearerTokenForMedia()
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

private fun String?.extractBearerTokenForMedia(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}
