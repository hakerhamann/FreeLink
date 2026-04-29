package com.freelink.feature.chat.ui.model

data class DirectMessageUiModel(
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timeLabel: String,
    val attachment: DirectMessageAttachmentUiModel? = null,
    val deliveryStateLabel: String? = null,
    val expiresAtLabel: String? = null,
    val replyToSnippet: String? = null,
    val reactions: Map<String, Int> = emptyMap()
)
