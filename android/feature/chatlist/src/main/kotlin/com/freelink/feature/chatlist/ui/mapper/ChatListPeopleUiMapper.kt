package com.freelink.feature.chatlist.ui.mapper

import com.freelink.core.model.domain.User
import com.freelink.feature.chatlist.ui.model.ChatListPersonUiModel

fun User.toChatListPersonUiModel(): ChatListPersonUiModel {
    return ChatListPersonUiModel(
        id = id,
        displayName = displayName,
        login = "@$login",
        status = if (isOnline) "online" else "offline",
        isOnline = isOnline
    )
}
