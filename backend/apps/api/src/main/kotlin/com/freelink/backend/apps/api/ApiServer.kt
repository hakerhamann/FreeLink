package com.freelink.backend.apps.api

import com.freelink.backend.apps.api.routes.installAuthRoutes
import com.freelink.backend.apps.api.routes.installChatRoutes
import com.freelink.backend.apps.api.routes.installGroupRoutes
import com.freelink.backend.apps.api.routes.installMediaRoutes
import com.freelink.backend.apps.api.routes.installMessageRoutes
import com.freelink.backend.apps.api.routes.installPeopleRoutes
import com.freelink.backend.apps.api.routes.installPrivacyRoutes
import com.freelink.backend.libs.auth.service.InMemoryAuthService
import com.freelink.backend.libs.chats.service.InMemoryChatsService
import com.freelink.backend.libs.groups.service.InMemoryGroupsService
import com.freelink.backend.libs.media.service.InMemoryMediaService
import com.freelink.backend.libs.messaging.service.InMemoryMessagingService
import com.freelink.backend.libs.people.service.InMemoryPeopleService
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
    val chatsService = InMemoryChatsService()
    val groupsService = InMemoryGroupsService()
    val mediaService = InMemoryMediaService()
    val messagingService = InMemoryMessagingService()
    val peopleService = InMemoryPeopleService()
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
        installChatRoutes(authService, chatsService)
        installGroupRoutes(authService, groupsService)
        installMediaRoutes(authService, mediaService)
        installMessageRoutes(authService, messagingService, privacySettingsService, chatsService)
        installPeopleRoutes(authService, peopleService)
        installPrivacyRoutes(authService, privacySettingsService)
    }
}
