package com.freelink.feature.chatlist.ui.mapper

import com.freelink.core.model.domain.Chat
import com.freelink.core.model.domain.ChatType
import com.freelink.feature.chatlist.ui.model.ChatListItemUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun Chat.toUiModel(): ChatListItemUiModel {
    val updatedAt = Instant.ofEpochMilli(updatedAtEpochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(timeFormatter)

    return ChatListItemUiModel(
        id = id,
        title = title.localizedChatTitle(),
        lastMessagePreview = lastMessagePreview.localizedPreview(),
        unreadCount = unreadCount,
        unreadBadge = when {
            unreadCount <= 0 -> null
            unreadCount > 99 -> "99+"
            else -> unreadCount.toString()
        },
        isPinned = isPinned,
        updatedAtLabel = updatedAt,
        chatTypeLabel = if (type == ChatType.DIRECT) "личный" else "группа"
    )
}

private fun String.localizedChatTitle(): String = when (this) {
    "Family" -> "Семья"
    "Lera" -> "Лера"
    "Artem" -> "Артём"
    "Work group" -> "Рабочая группа"
    "Travels" -> "Путешествия"
    "Weekend plans" -> "Планы на выходные"
    else -> this
}

private fun String.localizedPreview(): String = when (this) {
    "Do not forget dinner on Saturday." -> "Не забудьте, в субботу ужин у нас дома."
    "Thanks for your support." -> "Спасибо за поддержку, ты лучшая!"
    "I uploaded files to Space." -> "Скинул файлы в пространство."
    "Great, accepted." -> "Отлично, принято."
    else -> this
}
