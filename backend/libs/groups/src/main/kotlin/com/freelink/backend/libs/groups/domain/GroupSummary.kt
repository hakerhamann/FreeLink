package com.freelink.backend.libs.groups.domain

enum class GroupRole {
    OWNER,
    ADMIN,
    MEMBER
}

data class GroupSummary(
    val id: String,
    val title: String,
    val membersCount: Int,
    val myRole: GroupRole,
    val lastMessagePreview: String,
    val updatedAtEpochMs: Long
)
