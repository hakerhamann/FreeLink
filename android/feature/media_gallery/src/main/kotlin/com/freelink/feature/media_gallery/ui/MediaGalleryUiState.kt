package com.freelink.feature.media_gallery.ui

import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaUploadSession
import com.freelink.core.model.domain.UploadedMedia

data class MediaGalleryUiState(
    val fileName: String = "lake.jpg",
    val mimeType: String = "image/jpeg",
    val digestSha256: String = "sample-digest-001",
    val byteSizeText: String = "2048",
    val attachmentType: AttachmentType = AttachmentType.PHOTO,
    val isSubmitting: Boolean = false,
    val latestSession: MediaUploadSession? = null,
    val latestMedia: UploadedMedia? = null,
    val errorMessage: String? = null
)
