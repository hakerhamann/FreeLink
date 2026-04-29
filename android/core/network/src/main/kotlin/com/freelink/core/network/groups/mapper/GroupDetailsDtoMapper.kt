package com.freelink.core.network.groups.mapper

import com.freelink.core.model.domain.GroupDetails
import com.freelink.core.model.domain.GroupMember
import com.freelink.core.model.domain.GroupRole
import com.freelink.core.network.groups.dto.GroupDetailsDto

fun GroupDetailsDto.toDomain(): GroupDetails {
    return GroupDetails(
        summary = summary.toDomain(),
        members = members.map { member ->
            GroupMember(
                userId = member.userId,
                displayName = member.displayName,
                role = GroupRole.entries.firstOrNull { it.name == member.role } ?: GroupRole.MEMBER
            )
        }
    )
}
