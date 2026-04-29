package com.freelink.feature.group.data

import com.freelink.core.model.domain.GroupDetails
import com.freelink.core.model.domain.GroupSummary
import com.freelink.core.network.groups.GroupApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult

sealed interface GroupResult<out T> {
    data class Success<T>(val value: T) : GroupResult<T>
    data class Failure(val message: String) : GroupResult<Nothing>
}

class GroupRepository(
    private val authRepository: AuthRepository,
    private val groupApiClient: GroupApiClient
) {
    suspend fun loadGroups(query: String?): GroupResult<List<GroupSummary>> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                groupApiClient.fetchGroups(
                    accessToken = session.accessToken,
                    query = query
                )
            }
        ) {
            is AuthRepositoryResult.Success -> GroupResult.Success(result.value)
            is AuthRepositoryResult.Failure -> GroupResult.Failure(result.message)
        }
    }

    suspend fun loadGroupDetails(groupId: String): GroupResult<GroupDetails> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                groupApiClient.fetchGroupDetails(
                    accessToken = session.accessToken,
                    groupId = groupId
                )
            }
        ) {
            is AuthRepositoryResult.Success -> GroupResult.Success(result.value)
            is AuthRepositoryResult.Failure -> GroupResult.Failure(result.message)
        }
    }

    suspend fun createGroup(title: String): GroupResult<GroupSummary> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                groupApiClient.createGroup(
                    accessToken = session.accessToken,
                    title = title
                )
            }
        ) {
            is AuthRepositoryResult.Success -> GroupResult.Success(result.value)
            is AuthRepositoryResult.Failure -> GroupResult.Failure(result.message)
        }
    }
}
