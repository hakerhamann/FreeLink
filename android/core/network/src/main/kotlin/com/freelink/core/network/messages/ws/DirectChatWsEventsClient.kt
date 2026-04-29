package com.freelink.core.network.messages.ws

interface DirectChatWsEventsClient {
    suspend fun collectEvents(
        accessToken: String,
        chatId: String,
        onEvent: suspend (DirectChatWsEvent) -> Unit
    )
}
