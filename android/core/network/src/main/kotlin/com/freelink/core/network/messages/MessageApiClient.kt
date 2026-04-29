package com.freelink.core.network.messages

import com.freelink.core.model.domain.Message
import com.freelink.core.network.auth.AuthApiResult

interface MessageApiClient {
    suspend fun fetchMessages(accessToken: String, chatId: String, limit: Int? = null): AuthApiResult<List<Message>>

    suspend fun sendMessage(accessToken: String, chatId: String, body: String): AuthApiResult<Message>
}
