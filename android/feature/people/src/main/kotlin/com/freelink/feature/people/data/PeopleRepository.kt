package com.freelink.feature.people.data

import com.freelink.core.database.people.dao.PersonSummaryDao
import com.freelink.core.database.people.mapper.toDomain
import com.freelink.core.database.people.mapper.toEntity
import com.freelink.core.model.domain.User
import com.freelink.core.network.people.PeopleApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface PeopleResult<out T> {
    data class Success<T>(val value: T) : PeopleResult<T>
    data class Failure(val message: String) : PeopleResult<Nothing>
}

class PeopleRepository(
    private val authRepository: AuthRepository,
    private val peopleApiClient: PeopleApiClient,
    private val personSummaryDao: PersonSummaryDao
) {
    val peopleFlow: Flow<List<User>> = personSummaryDao.observeAll().map { items ->
        items.map { it.toDomain() }
    }

    suspend fun syncPeople(): PeopleResult<Unit> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                peopleApiClient.fetchPeople(session.accessToken)
            }
        ) {
            is AuthRepositoryResult.Success -> {
                personSummaryDao.clearAll()
                personSummaryDao.upsertAll(result.value.map { it.toEntity() })
                PeopleResult.Success(Unit)
            }

            is AuthRepositoryResult.Failure -> PeopleResult.Failure(result.message)
        }
    }
}
