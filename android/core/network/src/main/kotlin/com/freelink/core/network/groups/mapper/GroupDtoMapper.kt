package com.freelink.core.network.groups.mapper

import com.freelink.core.model.domain.GroupRole
import com.freelink.core.model.domain.GroupSummary
import com.freelink.core.network.groups.dto.GroupDto

fun GroupDto.toDomain(): GroupSummary {
    return GroupSummary(
        id = id,
        title = title,
        membersCount = membersCount,
        myRole = GroupRole.entries.firstOrNull { it.name == myRole } ?: GroupRole.MEMBER,
        lastMessagePreview = lastMessagePreview,
        updatedAtEpochMs = updatedAtEpochMs
    )
}
