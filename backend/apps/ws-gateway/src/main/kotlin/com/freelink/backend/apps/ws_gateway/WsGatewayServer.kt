package com.freelink.backend.apps.ws_gateway

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets

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
    }
}
