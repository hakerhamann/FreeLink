package com.freelink.feature.chat.ui.model

data class DirectMessageUiModel(
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timeLabel: String,
    val deliveryStateLabel: String? = null
)
