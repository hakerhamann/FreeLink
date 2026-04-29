package com.freelink.backend.libs.groups.domain

data class GroupMember(
    val userId: String,
    val displayName: String,
    val role: GroupRole
)
