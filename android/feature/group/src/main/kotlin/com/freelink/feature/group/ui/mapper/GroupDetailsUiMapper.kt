package com.freelink.feature.group.ui.mapper

import com.freelink.core.model.domain.GroupDetails
import com.freelink.feature.group.ui.model.GroupDetailsUiModel
import com.freelink.feature.group.ui.model.GroupMemberUiModel

fun GroupDetails.toUiModel(): GroupDetailsUiModel {
    return GroupDetailsUiModel(
        groupId = summary.id,
        title = summary.title,
        roleLabel = summary.myRole.name.lowercase().replaceFirstChar(Char::uppercase),
        membersLabel = "${summary.membersCount} members",
        subtitle = summary.lastMessagePreview,
        members = members.map { member ->
            GroupMemberUiModel(
                id = member.userId,
                displayName = member.displayName,
                roleLabel = member.role.name.lowercase().replaceFirstChar(Char::uppercase)
            )
        }
    )
}
