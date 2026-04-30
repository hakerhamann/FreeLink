package com.freelink.core.network.chatlist

import com.freelink.core.network.NetworkEndpoints

import com.freelink.core.model.domain.Chat
import com.freelink.core.network.auth.AuthApiResult
import com.freelink.core.network.chatlist.dto.ChatSummaryDto
import com.freelink.core.network.chatlist.mapper.toDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorChatListApiClient(
    private val baseUrl: String = NetworkEndpoints.ApiBaseUrl
) : ChatListApiClient {
    private val client: HttpClient = defaultClient()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun fetchChats(accessToken: String): AuthApiResult<List<Chat>> {
        val response = client.get("$baseUrl/chats") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to fetch chats",
                statusCode = response.status.value
            )
        }

        return AuthApiResult.Success(parseChatSummaries(response.body()))
    }

    override suspend fun fetchArchivedChats(accessToken: String): AuthApiResult<List<Chat>> {
        val response = client.get("$baseUrl/archive") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to fetch archived chats",
                statusCode = response.status.value
            )
        }

        return AuthApiResult.Success(parseChatSummaries(response.body()))
    }

    override suspend fun archiveChat(
        accessToken: String,
        chatId: String
    ): AuthApiResult<Unit> {
        val response = client.post("$baseUrl/archive/$chatId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to archive chat",
                statusCode = response.status.value
            )
        }

        return AuthApiResult.Success(Unit)
    }

    override suspend fun restoreArchivedChat(
        accessToken: String,
        chatId: String
    ): AuthApiResult<Unit> {
        val response = client.delete("$baseUrl/archive/$chatId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to restore archived chat",
                statusCode = response.status.value
            )
        }

        return AuthApiResult.Success(Unit)
    }

    private fun HttpStatusCode.isSuccess(): Boolean {
        return value in 200..299
    }

    private fun parseChatSummaries(bodyText: String): List<Chat> {
        val items = json.parseToJsonElement(bodyText) as? JsonArray ?: JsonArray(emptyList())
        return items.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            ChatSummaryDto(
                id = obj.stringOrEmpty("id"),
                title = obj.stringOrEmpty("title"),
                lastMessagePreview = obj.stringOrEmpty("lastMessagePreview"),
                type = obj.stringOrEmpty("type"),
                unreadCount = obj.intOrZero("unreadCount"),
                isPinned = obj.booleanOrFalse("isPinned"),
                updatedAtEpochMs = obj.longOrZero("updatedAtEpochMs")
            )
        }.map { it.toDomain() }
    }

    private fun JsonObject.stringOrEmpty(key: String): String {
        val value = this[key] as? JsonPrimitive
        return value?.content ?: ""
    }

    private fun JsonObject.intOrZero(key: String): Int {
        val value = this[key] as? JsonPrimitive
        return value?.content?.toIntOrNull() ?: 0
    }

    private fun JsonObject.longOrZero(key: String): Long {
        val value = this[key] as? JsonPrimitive
        return value?.content?.toLongOrNull() ?: 0L
    }

    private fun JsonObject.booleanOrFalse(key: String): Boolean {
        val value = this[key] as? JsonPrimitive
        return value?.content?.toBooleanStrictOrNull() ?: false
    }

    companion object {
        private fun defaultClient(): HttpClient {
            return HttpClient(CIO) {
                install(ContentNegotiation) {
                    json()
                }
                expectSuccess = false
                defaultRequest {
                    header(HttpHeaders.Accept, ContentType.Application.Json)
                }
            }
        }
    }
}
