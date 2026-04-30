package com.freelink.feature.people.ui.mapper

import com.freelink.core.model.domain.User
import com.freelink.feature.people.ui.model.PersonItemUiModel

fun User.toUiModel(): PersonItemUiModel {
    return PersonItemUiModel(
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
