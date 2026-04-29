package com.freelink.core.network.media.dto

data class MediaUploadSessionDto(
    val uploadId: String,
    val attachmentId: String?,
    val blobKey: String,
    val uploadUrl: String,
    val alreadyExists: Boolean
)

data class UploadedMediaDto(
    val attachmentId: String,
    val digestSha256: String,
    val byteSize: Long,
    val mimeType: String,
    val fileName: String,
    val attachmentType: String,
    val blobKey: String,
    val downloadUrl: String
)
