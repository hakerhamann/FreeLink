package com.freelink.core.network.chatlist.ws

interface ChatListWsEventsClient {
    suspend fun collectChatUpdatedEvents(
        accessToken: String,
        onChatUpdated: suspend () -> Unit
    )
}
