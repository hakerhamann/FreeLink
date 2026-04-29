package com.freelink.backend.libs.media.service

import com.freelink.backend.libs.media.domain.MediaUploadSession
import com.freelink.backend.libs.media.domain.StoredMedia
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryMediaService : MediaService {
    private val uploadsById = ConcurrentHashMap<String, PendingUpload>()
    private val mediaByDigest = ConcurrentHashMap<String, StoredMedia>()

    override fun initUpload(userId: String, command: MediaInitCommand): MediaUploadSession {
        val normalizedDigest = command.digestSha256.trim()
        mediaByDigest[normalizedDigest]?.let { existing ->
            val uploadId = "upload-${UUID.randomUUID().toString().take(8)}"
            uploadsById[uploadId] = PendingUpload(
                userId = userId,
                digestSha256 = normalizedDigest,
                byteSize = existing.byteSize,
                mimeType = existing.mimeType,
                fileName = existing.fileName,
                attachmentType = existing.attachmentType,
                blobKey = existing.blobKey,
                alreadyExists = true
            )
            return MediaUploadSession(
                uploadId = uploadId,
                attachmentId = existing.attachmentId,
                blobKey = existing.blobKey,
                uploadUrl = "",
                alreadyExists = true
            )
        }

        val uploadId = "upload-${UUID.randomUUID().toString().take(8)}"
        val blobKey = "media/${userId.takeLast(6)}/$uploadId/${command.fileName.trim()}"
        uploadsById[uploadId] = PendingUpload(
            userId = userId,
            digestSha256 = normalizedDigest,
            byteSize = command.byteSize,
            mimeType = command.mimeType.trim(),
            fileName = command.fileName.trim(),
            attachmentType = command.attachmentType.trim(),
            blobKey = blobKey,
            alreadyExists = false
        )

        return MediaUploadSession(
            uploadId = uploadId,
            attachmentId = null,
            blobKey = blobKey,
            uploadUrl = "https://staging.freelink.local/upload/$uploadId",
            alreadyExists = false
        )
    }

    override fun completeUpload(userId: String, uploadId: String): StoredMedia? {
        val pending = uploadsById[uploadId] ?: return null
        if (pending.userId != userId) {
            return null
        }

        val existing = mediaByDigest[pending.digestSha256]
        if (existing != null) {
            return existing
        }

        val stored = StoredMedia(
            attachmentId = "media-${UUID.randomUUID().toString().take(8)}",
            digestSha256 = pending.digestSha256,
            byteSize = pending.byteSize,
            mimeType = pending.mimeType,
            fileName = pending.fileName,
            attachmentType = pending.attachmentType,
            blobKey = pending.blobKey,
            downloadUrl = "https://staging.freelink.local/blob/${pending.blobKey}"
        )
        mediaByDigest[pending.digestSha256] = stored
        return stored
    }

    private data class PendingUpload(
        val userId: String,
        val digestSha256: String,
        val byteSize: Long,
        val mimeType: String,
        val fileName: String,
        val attachmentType: String,
        val blobKey: String,
        val alreadyExists: Boolean
    )
}
