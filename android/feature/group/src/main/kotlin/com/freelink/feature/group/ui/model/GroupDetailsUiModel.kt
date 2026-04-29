package com.freelink.feature.group.ui.model

data class GroupDetailsUiModel(
    val groupId: String,
    val title: String,
    val roleLabel: String,
    val membersLabel: String,
    val subtitle: String,
    val members: List<GroupMemberUiModel>
)
