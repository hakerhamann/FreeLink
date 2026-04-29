package com.freelink.backend.libs.media.service

import com.freelink.backend.libs.media.domain.MediaUploadSession
import com.freelink.backend.libs.media.domain.StoredMedia

data class MediaInitCommand(
    val fileName: String,
    val mimeType: String,
    val digestSha256: String,
    val byteSize: Long,
    val attachmentType: String
)

interface MediaService {
    fun initUpload(userId: String, command: MediaInitCommand): MediaUploadSession

    fun completeUpload(userId: String, uploadId: String): StoredMedia?
}
