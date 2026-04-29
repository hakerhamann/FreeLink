package com.freelink.feature.archive.data

import com.freelink.core.model.domain.Chat
import com.freelink.core.network.chatlist.ChatListApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult

sealed interface ArchiveResult<out T> {
    data class Success<T>(val value: T) : ArchiveResult<T>
    data class Failure(val message: String) : ArchiveResult<Nothing>
}

class ArchiveRepository(
    private val authRepository: AuthRepository,
    private val chatListApiClient: ChatListApiClient
) {
    suspend fun loadArchivedChats(): ArchiveResult<List<Chat>> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                chatListApiClient.fetchArchivedChats(session.accessToken)
            }
        ) {
            is AuthRepositoryResult.Success -> ArchiveResult.Success(result.value)
            is AuthRepositoryResult.Failure -> ArchiveResult.Failure(result.message)
        }
    }

    suspend fun restoreArchivedChat(chatId: String): ArchiveResult<Unit> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                chatListApiClient.restoreArchivedChat(
                    accessToken = session.accessToken,
                    chatId = chatId
                )
            }
        ) {
            is AuthRepositoryResult.Success -> ArchiveResult.Success(Unit)
            is AuthRepositoryResult.Failure -> ArchiveResult.Failure(result.message)
        }
    }
}
