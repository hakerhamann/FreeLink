package com.freelink.core.network.chatlist

import com.freelink.core.model.domain.Chat
import com.freelink.core.network.auth.AuthApiResult

interface ChatListApiClient {
    suspend fun fetchChats(accessToken: String): AuthApiResult<List<Chat>>

    suspend fun archiveChat(
        accessToken: String,
        chatId: String
    ): AuthApiResult<Unit>
}
