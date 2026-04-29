package com.freelink.feature.chatlist.data

import com.freelink.core.database.chatlist.dao.ChatSummaryDao
import com.freelink.core.database.chatlist.mapper.toDomain
import com.freelink.core.database.chatlist.mapper.toEntity
import com.freelink.core.model.domain.Chat
import com.freelink.core.model.domain.User
import com.freelink.core.network.chatlist.ChatListApiClient
import com.freelink.core.network.chatlist.ws.ChatListWsEventsClient
import com.freelink.core.database.people.dao.PersonSummaryDao
import com.freelink.core.database.people.mapper.toDomain as personToDomain
import com.freelink.core.database.people.mapper.toEntity as personToEntity
import com.freelink.core.network.people.PeopleApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface ChatListResult<out T> {
    data class Success<T>(val value: T) : ChatListResult<T>
    data class Failure(val message: String) : ChatListResult<Nothing>
}

class ChatListRepository(
    private val authRepository: AuthRepository,
    private val chatListApiClient: ChatListApiClient,
    private val chatListWsEventsClient: ChatListWsEventsClient,
    private val chatSummaryDao: ChatSummaryDao,
    private val peopleApiClient: PeopleApiClient,
    private val personSummaryDao: PersonSummaryDao
) {
    val chatsFlow: Flow<List<Chat>> = chatSummaryDao.observeAll().map { items ->
        items.map { it.toDomain() }
    }

    val peopleFlow: Flow<List<User>> = personSummaryDao.observeAll().map { items ->
        items.map { it.personToDomain() }
    }

    suspend fun syncChats(): ChatListResult<Unit> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                chatListApiClient.fetchChats(session.accessToken)
            }
        ) {
            is AuthRepositoryResult.Success -> {
                chatSummaryDao.clearAll()
                chatSummaryDao.upsertAll(result.value.map { it.toEntity() })
                ChatListResult.Success(Unit)
            }
            is AuthRepositoryResult.Failure -> ChatListResult.Failure(result.message)
        }
    }

    suspend fun syncPeople(): ChatListResult<Unit> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                peopleApiClient.fetchPeople(session.accessToken)
            }
        ) {
            is AuthRepositoryResult.Success -> {
                personSummaryDao.clearAll()
                personSummaryDao.upsertAll(result.value.map { it.personToEntity() })
                ChatListResult.Success(Unit)
            }
            is AuthRepositoryResult.Failure -> ChatListResult.Failure(result.message)
        }
    }

    suspend fun observeRemoteUpdates() {
        var reconnectAttempt = 0

        while (true) {
            val accessToken = authRepository.currentAccessToken() ?: return

            try {
                chatListWsEventsClient.collectChatUpdatedEvents(
                    accessToken = accessToken
                ) {
                    syncChats()
                }
                reconnectAttempt = 0
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                reconnectAttempt += 1
                val backoffMs = (reconnectAttempt * 1_000L).coerceAtMost(5_000L)
                delay(backoffMs)
            }
        }
    }
}
