package com.freelink.feature.media_gallery.data

import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaUploadSession
import com.freelink.core.model.domain.UploadedMedia
import com.freelink.core.network.media.MediaApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult

sealed interface MediaGalleryResult<out T> {
    data class Success<T>(val value: T) : MediaGalleryResult<T>
    data class Failure(val message: String) : MediaGalleryResult<Nothing>
}

class MediaGalleryRepository(
    private val authRepository: AuthRepository,
    private val mediaApiClient: MediaApiClient
) {
    suspend fun initUpload(
        fileName: String,
        mimeType: String,
        digestSha256: String,
        byteSize: Long,
        attachmentType: AttachmentType
    ): MediaGalleryResult<MediaUploadSession> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                mediaApiClient.initUpload(
                    accessToken = session.accessToken,
                    fileName = fileName,
                    mimeType = mimeType,
                    digestSha256 = digestSha256,
                    byteSize = byteSize,
                    attachmentType = attachmentType
                )
            }
        ) {
            is AuthRepositoryResult.Success -> MediaGalleryResult.Success(result.value)
            is AuthRepositoryResult.Failure -> MediaGalleryResult.Failure(result.message)
        }
    }

    suspend fun completeUpload(uploadId: String): MediaGalleryResult<UploadedMedia> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                mediaApiClient.completeUpload(
                    accessToken = session.accessToken,
                    uploadId = uploadId
                )
            }
        ) {
            is AuthRepositoryResult.Success -> MediaGalleryResult.Success(result.value)
            is AuthRepositoryResult.Failure -> MediaGalleryResult.Failure(result.message)
        }
    }
}
