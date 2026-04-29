package com.freelink.core.network.messages.ws

sealed interface DirectChatWsEvent {
    data class MessageCreated(val messageId: String?) : DirectChatWsEvent
    object TypingStarted : DirectChatWsEvent
    object TypingStopped : DirectChatWsEvent
    data class ReceiptDelivered(val messageId: String?) : DirectChatWsEvent
    data class ReceiptRead(val messageId: String?) : DirectChatWsEvent
}
