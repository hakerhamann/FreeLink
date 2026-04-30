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
    return "\u0418\u0441\u0447\u0435\u0437\u043d\u0435\u0442 \u0432 $expiryTime"
}

private fun String.extractFirstUrl(): String? {
    return urlPattern.find(this)?.value
}

private val urlPattern = Regex("""https?://\S+""")

private fun AttachmentType.typeLabel(): String {
    return when (this) {
        AttachmentType.PHOTO -> "\u0424\u043e\u0442\u043e"
        AttachmentType.VIDEO -> "\u0412\u0438\u0434\u0435\u043e"
        AttachmentType.VOICE -> "\u0413\u043e\u043b\u043e\u0441"
        AttachmentType.FILE -> "\u0424\u0430\u0439\u043b"
    }
}

fun MediaAttachment.toUiModel(): DirectMessageAttachmentUiModel {
    val attachmentKind = type.toUiKind()
    return DirectMessageAttachmentUiModel(
        id = id,
        kind = attachmentKind,
        typeLabel = type.typeLabel(),
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
        DirectMessageAttachmentKindUiModel.PHOTO -> "\u041f\u0440\u0435\u0434\u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u0444\u043e\u0442\u043e"
        DirectMessageAttachmentKindUiModel.VIDEO -> "\u041f\u0440\u0435\u0434\u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u0432\u0438\u0434\u0435\u043e"
        DirectMessageAttachmentKindUiModel.VOICE -> "\u0413\u043e\u043b\u043e\u0441\u043e\u0432\u043e\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435"
        DirectMessageAttachmentKindUiModel.FILE -> "\u0424\u0430\u0439\u043b"
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
