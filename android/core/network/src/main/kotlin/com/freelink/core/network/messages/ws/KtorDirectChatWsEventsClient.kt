package com.freelink.core.network.messages.ws

import com.freelink.core.network.NetworkEndpoints

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorDirectChatWsEventsClient(
    private val wsUrl: String = NetworkEndpoints.DirectChatWsUrl
) : DirectChatWsEventsClient {
    private val client: HttpClient = defaultClient()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun collectEvents(
        accessToken: String,
        chatId: String,
        onEvent: suspend (DirectChatWsEvent) -> Unit
    ) {
        val session = client.webSocketSession {
            url(wsUrl)
            parameter("chatId", chatId)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        for (frame in session.incoming) {
            val text = (frame as? Frame.Text)?.readText() ?: continue
            val event = parseEvent(text) ?: continue
            onEvent(event)
        }
    }

    private fun parseEvent(payload: String): DirectChatWsEvent? {
        val root = runCatching { json.parseToJsonElement(payload) as? JsonObject }.getOrNull() ?: return null
        val type = (root["type"] as? JsonPrimitive)?.content ?: return null
        val messageId = (root["messageId"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }

        return when (type) {
            "message.created" -> DirectChatWsEvent.MessageCreated(messageId)
            "typing.started" -> DirectChatWsEvent.TypingStarted
            "typing.stopped" -> DirectChatWsEvent.TypingStopped
            "receipt.delivered" -> DirectChatWsEvent.ReceiptDelivered(messageId)
            "receipt.read" -> DirectChatWsEvent.ReceiptRead(messageId)
            else -> null
        }
    }

    companion object {
        private fun defaultClient(): HttpClient {
            return HttpClient(CIO) {
                install(ContentNegotiation) {
                    json()
                }
                install(WebSockets)
            }
        }
    }
}
