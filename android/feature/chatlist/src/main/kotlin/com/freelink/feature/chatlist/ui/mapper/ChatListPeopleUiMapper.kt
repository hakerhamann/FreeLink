package com.freelink.feature.chatlist.ui.mapper

import com.freelink.core.model.domain.User
import com.freelink.feature.chatlist.ui.model.ChatListPersonUiModel

fun User.toChatListPersonUiModel(): ChatListPersonUiModel {
    return ChatListPersonUiModel(
        id = id,
        displayName = displayName.localizedName(),
        login = "@$login",
        status = if (isOnline) "в сети" else "не в сети",
        isOnline = isOnline
    )
}

private fun String.localizedName(): String = when (this) {
    "Lera" -> "Лера"
    "Artem" -> "Артём"
    "Masha" -> "Маша"
    "Ilya" -> "Илья"
    else -> this
}
