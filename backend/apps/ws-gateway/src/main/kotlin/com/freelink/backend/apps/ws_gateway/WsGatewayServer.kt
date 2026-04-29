package com.freelink.backend.apps.ws_gateway

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.delay

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::freeLinkWsGatewayModule)
        .start(wait = true)
}

fun Application.freeLinkWsGatewayModule() {
    install(WebSockets)

    routing {
        get("/health") {
            call.respondText("ok")
        }

        webSocket("/ws/chats") {
            val accessToken = call.request.headers[HttpHeaders.Authorization].extractBearerTokenForWsGateway()
            if (accessToken.isNullOrBlank()) {
                return@webSocket
            }

            while (true) {
                val payload = """{"type":"chat.updated","chatId":"chat-family","sentAtEpochMs":${System.currentTimeMillis()}}"""
                outgoing.send(Frame.Text(payload))
                delay(15_000)
            }
        }

        webSocket("/ws/messages") {
            val accessToken = call.request.headers[HttpHeaders.Authorization].extractBearerTokenForWsGateway()
            val chatId = call.request.queryParameters["chatId"]?.trim().orEmpty()
            if (accessToken.isNullOrBlank() || chatId.isBlank()) {
                return@webSocket
            }

            var sequence = 0
            while (true) {
                delay(4_000)
                outgoing.send(Frame.Text(buildWsPayload(type = "typing.started", chatId = chatId)))
                delay(1_200)
                outgoing.send(Frame.Text(buildWsPayload(type = "typing.stopped", chatId = chatId)))

                val createdMessageId = "$chatId-ws-$sequence-${System.currentTimeMillis()}"
                outgoing.send(
                    Frame.Text(
                        buildWsPayload(
                            type = "message.created",
                            chatId = chatId,
                            messageId = createdMessageId
                        )
                    )
                )

                delay(800)
                outgoing.send(Frame.Text(buildWsPayload(type = "receipt.delivered", chatId = chatId)))
                delay(800)
                outgoing.send(Frame.Text(buildWsPayload(type = "receipt.read", chatId = chatId)))

                sequence += 1
                delay(12_000)
            }
        }
    }
}

private fun String?.extractBearerTokenForWsGateway(): String? {
    if (this == null) {
        return null
    }

    val prefix = "Bearer "
    return if (startsWith(prefix)) substring(prefix.length).trim() else null
}

private fun buildWsPayload(
    type: String,
    chatId: String,
    messageId: String? = null
): String {
    val messagePart = if (messageId == null) "" else ",\"messageId\":\"$messageId\""
    return """{"type":"$type","chatId":"$chatId"$messagePart,"sentAtEpochMs":${System.currentTimeMillis()}}"""
}
