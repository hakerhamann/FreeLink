package com.freelink.core.network.media.mapper

import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaAttachment
import com.freelink.core.model.domain.MediaUploadSession
import com.freelink.core.model.domain.UploadedMedia
import com.freelink.core.network.media.dto.MediaUploadSessionDto
import com.freelink.core.network.media.dto.UploadedMediaDto

fun MediaUploadSessionDto.toDomain(): MediaUploadSession {
    return MediaUploadSession(
        uploadId = uploadId,
        attachmentId = attachmentId,
        blobKey = blobKey,
        uploadUrl = uploadUrl,
        alreadyExists = alreadyExists
    )
}

fun UploadedMediaDto.toDomain(): UploadedMedia {
    return UploadedMedia(
        attachmentId = attachmentId,
        digestSha256 = digestSha256,
        byteSize = byteSize,
        mimeType = mimeType,
        fileName = fileName,
        attachmentType = AttachmentType.entries.firstOrNull { it.name == attachmentType } ?: AttachmentType.FILE,
        blobKey = blobKey,
        downloadUrl = downloadUrl
    )
}

fun UploadedMedia.toAttachment(): MediaAttachment {
    return MediaAttachment(
        id = attachmentId,
        type = attachmentType,
        fileName = fileName,
        digestSha256 = digestSha256,
        byteSize = byteSize,
        mimeType = mimeType,
        downloadUrl = downloadUrl
    )
}
