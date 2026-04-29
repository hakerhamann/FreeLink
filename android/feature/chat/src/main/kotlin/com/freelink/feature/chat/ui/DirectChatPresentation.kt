package com.freelink.feature.chat.ui

import com.freelink.feature.chat.ui.model.AttachmentTrayActionUiModel

private const val PHOTO_ACTION_ID = "photo"
private const val FILE_ACTION_ID = "file"
private const val VOICE_ACTION_ID = "voice"

fun defaultAttachmentTrayActions(): List<AttachmentTrayActionUiModel> {
    return listOf(
        AttachmentTrayActionUiModel(
            id = PHOTO_ACTION_ID,
            label = "Photo"
        ),
        AttachmentTrayActionUiModel(
            id = FILE_ACTION_ID,
            label = "File"
        ),
        AttachmentTrayActionUiModel(
            id = VOICE_ACTION_ID,
            label = "Voice"
        )
    )
}

fun attachmentTypeForAction(actionId: String): com.freelink.core.model.domain.AttachmentType? {
    return when (actionId) {
        PHOTO_ACTION_ID -> com.freelink.core.model.domain.AttachmentType.PHOTO
        FILE_ACTION_ID -> com.freelink.core.model.domain.AttachmentType.FILE
        VOICE_ACTION_ID -> com.freelink.core.model.domain.AttachmentType.VOICE
        else -> null
    }
}
