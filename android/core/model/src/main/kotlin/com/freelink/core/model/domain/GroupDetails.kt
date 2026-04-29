package com.freelink.core.model.domain

data class GroupMember(
    val userId: String,
    val displayName: String,
    val role: GroupRole
)

data class GroupDetails(
    val summary: GroupSummary,
    val members: List<GroupMember>
)
