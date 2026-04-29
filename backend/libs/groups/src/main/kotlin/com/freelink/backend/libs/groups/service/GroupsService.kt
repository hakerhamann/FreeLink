package com.freelink.backend.libs.groups.service

import com.freelink.backend.libs.groups.domain.GroupDetails
import com.freelink.backend.libs.groups.domain.GroupSummary

data class CreateGroupCommand(
    val title: String,
    val memberUserIds: List<String>
)

interface GroupsService {
    fun listGroups(userId: String, query: String?): List<GroupSummary>

    fun getGroupDetails(userId: String, groupId: String): GroupDetails?

    fun createGroup(ownerUserId: String, command: CreateGroupCommand): GroupSummary
}
