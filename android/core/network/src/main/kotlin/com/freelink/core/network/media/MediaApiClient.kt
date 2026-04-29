package com.freelink.core.network.media

import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaUploadSession
import com.freelink.core.model.domain.UploadedMedia
import com.freelink.core.network.auth.AuthApiResult

interface MediaApiClient {
    suspend fun initUpload(
        accessToken: String,
        fileName: String,
        mimeType: String,
        digestSha256: String,
        byteSize: Long,
        attachmentType: AttachmentType
    ): AuthApiResult<MediaUploadSession>

    suspend fun completeUpload(
        accessToken: String,
        uploadId: String
    ): AuthApiResult<UploadedMedia>
}
