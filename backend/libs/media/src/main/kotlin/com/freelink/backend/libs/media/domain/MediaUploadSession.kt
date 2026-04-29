package com.freelink.backend.libs.media.domain

data class MediaUploadSession(
    val uploadId: String,
    val attachmentId: String?,
    val blobKey: String,
    val uploadUrl: String,
    val alreadyExists: Boolean
)
