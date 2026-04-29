package com.freelink.feature.group.ui.mapper

import com.freelink.core.model.domain.GroupSummary
import com.freelink.feature.group.ui.model.GroupItemUiModel

fun GroupSummary.toUiModel(): GroupItemUiModel {
    return GroupItemUiModel(
        id = id,
        title = title,
        subtitle = lastMessagePreview,
        roleLabel = myRole.name.lowercase().replaceFirstChar(Char::uppercase),
        membersLabel = "$membersCount members"
    )
}
