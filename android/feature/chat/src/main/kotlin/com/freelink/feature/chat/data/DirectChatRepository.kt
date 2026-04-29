package com.freelink.feature.chat.data

import com.freelink.core.model.domain.Message
import com.freelink.core.network.messages.MessageApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.auth.data.AuthRepositoryResult

sealed interface DirectChatResult<out T> {
    data class Success<T>(val value: T) : DirectChatResult<T>
    data class Failure(val message: String) : DirectChatResult<Nothing>
}

class DirectChatRepository(
    private val authRepository: AuthRepository,
    private val messageApiClient: MessageApiClient
) {
    suspend fun currentUserId(): String? {
        return authRepository.currentUserId()
    }

    suspend fun loadMessages(chatId: String): DirectChatResult<List<Message>> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                messageApiClient.fetchMessages(
                    accessToken = session.accessToken,
                    chatId = chatId
                )
            }
        ) {
            is AuthRepositoryResult.Success -> DirectChatResult.Success(result.value)
            is AuthRepositoryResult.Failure -> DirectChatResult.Failure(result.message)
        }
    }

    suspend fun sendMessage(chatId: String, body: String): DirectChatResult<Message> {
        return when (
            val result = authRepository.authorizedRequest { session ->
                messageApiClient.sendMessage(
                    accessToken = session.accessToken,
                    chatId = chatId,
                    body = body
                )
            }
        ) {
            is AuthRepositoryResult.Success -> DirectChatResult.Success(result.value)
            is AuthRepositoryResult.Failure -> DirectChatResult.Failure(result.message)
        }
    }
}
