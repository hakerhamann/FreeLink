package com.freelink.core.model.domain

enum class AttachmentType {
    PHOTO,
    VIDEO,
    VOICE,
    FILE
}

data class MediaAttachment(
    val id: String,
    val type: AttachmentType,
    val digestSha256: String,
    val byteSize: Long,
    val mimeType: String
)
