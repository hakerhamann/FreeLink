package com.freelink.feature.chat.ui.mapper

import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaAttachment
import com.freelink.core.model.domain.Message
import com.freelink.feature.chat.ui.model.DirectMessageAttachmentKindUiModel
import com.freelink.feature.chat.ui.model.DirectMessageAttachmentUiModel
import com.freelink.feature.chat.ui.model.DirectMessageUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.max

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
        expiresAtLabel = expiresAtEpochMs?.toExpiryLabel(),
        linkPreviewUrl = body.extractFirstUrl(),
        replyToSnippet = replyToSnippet,
        reactions = reactions
    )
}

private fun Long.toExpiryLabel(): String {
    val expiryTime = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(timeFormatter)
    return "Expires $expiryTime"
}

private fun String.extractFirstUrl(): String? {
    return urlPattern.find(this)?.value
}

private val urlPattern = Regex("""https?://\S+""")

fun MediaAttachment.toUiModel(): DirectMessageAttachmentUiModel {
    val attachmentKind = type.toUiKind()
    return DirectMessageAttachmentUiModel(
        id = id,
        kind = attachmentKind,
        typeLabel = type.name.lowercase().replaceFirstChar(Char::uppercase),
        fileName = fileName,
        sizeLabel = formatAttachmentSize(byteSize),
        downloadUrl = downloadUrl,
        previewLabel = previewLabelFor(attachmentKind),
        durationLabel = if (attachmentKind == DirectMessageAttachmentKindUiModel.VOICE) {
            formatVoiceDuration(byteSize)
        } else {
            null
        },
        waveformBars = if (attachmentKind == DirectMessageAttachmentKindUiModel.VOICE) {
            buildWaveformBars(id = id, digest = digestSha256)
        } else {
            emptyList()
        }
    )
}

private fun previewLabelFor(kind: DirectMessageAttachmentKindUiModel): String {
    return when (kind) {
        DirectMessageAttachmentKindUiModel.PHOTO -> "Photo preview"
        DirectMessageAttachmentKindUiModel.VIDEO -> "Video preview"
        DirectMessageAttachmentKindUiModel.VOICE -> "Voice message"
        DirectMessageAttachmentKindUiModel.FILE -> "File attachment"
    }
}

private fun AttachmentType.toUiKind(): DirectMessageAttachmentKindUiModel {
    return when (this) {
        AttachmentType.PHOTO -> DirectMessageAttachmentKindUiModel.PHOTO
        AttachmentType.VIDEO -> DirectMessageAttachmentKindUiModel.VIDEO
        AttachmentType.VOICE -> DirectMessageAttachmentKindUiModel.VOICE
        AttachmentType.FILE -> DirectMessageAttachmentKindUiModel.FILE
    }
}

private fun formatAttachmentSize(byteSize: Long): String {
    if (byteSize >= 1_048_576) {
        return String.format(Locale.US, "%.1f MB", byteSize / 1_048_576f)
    }
    if (byteSize >= 1024) {
        return String.format(Locale.US, "%.1f KB", byteSize / 1024f)
    }
    return "$byteSize B"
}

private fun formatVoiceDuration(byteSize: Long): String {
    val totalSeconds = max(3, (byteSize / 512L).toInt())
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun buildWaveformBars(
    id: String,
    digest: String
): List<Int> {
    val seed = "$id-$digest".hashCode().absoluteValue
    return List(12) { index ->
        16 + ((seed shr (index % 8)) + index * 11).absoluteValue % 22
    }
}
