package com.freelink.feature.chat.ui.model

enum class DirectMessageAttachmentKindUiModel {
    PHOTO,
    VIDEO,
    VOICE,
    FILE
}

data class DirectMessageAttachmentUiModel(
    val id: String,
    val kind: DirectMessageAttachmentKindUiModel,
    val typeLabel: String,
    val fileName: String,
    val sizeLabel: String,
    val downloadUrl: String,
    val durationLabel: String? = null,
    val waveformBars: List<Int> = emptyList()
)
