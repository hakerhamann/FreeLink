package com.freelink.core.network.chatlist.ws

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorChatListWsEventsClient(
    private val wsUrl: String = "ws://10.0.2.2:8081/ws/chats"
) : ChatListWsEventsClient {
    private val client: HttpClient = defaultClient()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun collectChatUpdatedEvents(
        accessToken: String,
        onChatUpdated: suspend () -> Unit
    ) {
        val session = client.webSocketSession {
            url(wsUrl)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        for (frame in session.incoming) {
            val textFrame = frame as? Frame.Text ?: continue
            val type = extractType(textFrame.readText())
            if (type == "chat.updated") {
                onChatUpdated()
            }
        }
    }

    private fun extractType(payload: String): String? {
        return runCatching {
            val root = json.parseToJsonElement(payload) as? JsonObject
            val type = root?.get("type") as? JsonPrimitive
            type?.content
        }.getOrNull()
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
