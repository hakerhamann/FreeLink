package com.freelink.feature.people.ui.mapper

import com.freelink.core.model.domain.User
import com.freelink.feature.people.ui.model.PersonItemUiModel

fun User.toUiModel(): PersonItemUiModel {
    return PersonItemUiModel(
        id = id,
        displayName = displayName,
        login = "@$login",
        status = if (isOnline) "online" else "offline",
        isOnline = isOnline
    )
}
