package com.freelink.feature.chat.ui.mapper

import com.freelink.core.model.domain.MediaAttachment
import com.freelink.core.model.domain.Message
import com.freelink.feature.chat.ui.model.DirectMessageAttachmentUiModel
import com.freelink.feature.chat.ui.model.DirectMessageUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun Message.toDirectUiModel(
    currentUserId: String?,
    replyToSnippet: String?
): DirectMessageUiModel {
    val timeLabel = Instant.ofEpochMilli(createdAtEpochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(timeFormatter)

    return DirectMessageUiModel(
        id = id,
        text = body,
        isOutgoing = senderUserId == currentUserId,
        timeLabel = timeLabel,
        attachment = attachment?.toUiModel(),
        deliveryStateLabel = deliveryState.name.lowercase(),
        replyToSnippet = replyToSnippet,
        reactions = reactions
    )
}

fun MediaAttachment.toUiModel(): DirectMessageAttachmentUiModel {
    return DirectMessageAttachmentUiModel(
        id = id,
        typeLabel = type.name.lowercase().replaceFirstChar(Char::uppercase),
        fileName = fileName,
        sizeLabel = formatAttachmentSize(byteSize),
        downloadUrl = downloadUrl
    )
}

private fun formatAttachmentSize(byteSize: Long): String {
    if (byteSize >= 1_048_576) {
        return String.format("%.1f MB", byteSize / 1_048_576f)
    }
    if (byteSize >= 1024) {
        return String.format("%.1f KB", byteSize / 1024f)
    }
    return "$byteSize B"
}
