package com.freelink.core.model.domain

data class MediaUploadSession(
    val uploadId: String,
    val attachmentId: String?,
    val blobKey: String,
    val uploadUrl: String,
    val alreadyExists: Boolean
)

data class UploadedMedia(
    val attachmentId: String,
    val digestSha256: String,
    val byteSize: Long,
    val mimeType: String,
    val fileName: String,
    val attachmentType: AttachmentType,
    val blobKey: String,
    val downloadUrl: String
)
