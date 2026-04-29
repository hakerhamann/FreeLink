package com.freelink.backend.libs.messaging.domain

data class MessageAttachment(
    val id: String,
    val type: String,
    val fileName: String,
    val digestSha256: String,
    val byteSize: Long,
    val mimeType: String,
    val downloadUrl: String
)
