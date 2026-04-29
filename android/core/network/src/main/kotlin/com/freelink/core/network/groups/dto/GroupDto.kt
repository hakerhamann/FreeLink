package com.freelink.core.network.groups.dto

data class GroupDto(
    val id: String,
    val title: String,
    val membersCount: Int,
    val myRole: String,
    val lastMessagePreview: String,
    val updatedAtEpochMs: Long
)
