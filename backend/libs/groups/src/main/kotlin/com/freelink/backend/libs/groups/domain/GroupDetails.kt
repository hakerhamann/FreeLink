package com.freelink.backend.libs.groups.domain

data class GroupDetails(
    val summary: GroupSummary,
    val members: List<GroupMember>
)
