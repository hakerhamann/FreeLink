package com.freelink.backend.apps.api

import com.freelink.backend.apps.api.routes.installAuthRoutes
import com.freelink.backend.apps.api.routes.installPrivacyRoutes
import com.freelink.backend.libs.auth.service.InMemoryAuthService
import com.freelink.backend.libs.privacy.service.InMemoryPrivacySettingsService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::freeLinkApiModule)
        .start(wait = true)
}

fun Application.freeLinkApiModule() {
    val authService = InMemoryAuthService()
    val privacySettingsService = InMemoryPrivacySettingsService()

    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/health") {
            call.respondText("ok")
        }
        get("/api/v1/ping") {
            call.respondText("freelink-api")
        }
        installAuthRoutes(authService)
        installPrivacyRoutes(authService, privacySettingsService)
    }
}
