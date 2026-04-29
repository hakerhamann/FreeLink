package com.freelink.core.network.groups

import com.freelink.core.model.domain.GroupDetails
import com.freelink.core.model.domain.GroupSummary
import com.freelink.core.network.auth.AuthApiResult

interface GroupApiClient {
    suspend fun fetchGroups(accessToken: String, query: String? = null): AuthApiResult<List<GroupSummary>>

    suspend fun fetchGroupDetails(accessToken: String, groupId: String): AuthApiResult<GroupDetails>

    suspend fun createGroup(
        accessToken: String,
        title: String,
        memberUserIds: List<String> = emptyList()
    ): AuthApiResult<GroupSummary>
}
