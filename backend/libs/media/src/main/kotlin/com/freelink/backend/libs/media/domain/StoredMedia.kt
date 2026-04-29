package com.freelink.backend.libs.media.domain

data class StoredMedia(
    val attachmentId: String,
    val digestSha256: String,
    val byteSize: Long,
    val mimeType: String,
    val fileName: String,
    val attachmentType: String,
    val blobKey: String,
    val downloadUrl: String
)
