package com.freelink.core.network.groups.dto

data class GroupMemberDto(
    val userId: String,
    val displayName: String,
    val role: String
)

data class GroupDetailsDto(
    val summary: GroupDto,
    val members: List<GroupMemberDto>
)
