package com.freelink.feature.chat.ui.mapper

import com.freelink.core.model.domain.Message
import com.freelink.feature.chat.ui.model.DirectMessageUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun Message.toDirectUiModel(currentUserId: String?): DirectMessageUiModel {
    val timeLabel = Instant.ofEpochMilli(createdAtEpochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(timeFormatter)

    return DirectMessageUiModel(
        id = id,
        text = body,
        isOutgoing = senderUserId == currentUserId,
        timeLabel = timeLabel,
        deliveryStateLabel = deliveryState.name.lowercase()
    )
}
