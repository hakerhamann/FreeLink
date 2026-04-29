package com.freelink.core.network.messages

import com.freelink.core.model.domain.Message
import com.freelink.core.network.auth.AuthApiResult
import com.freelink.core.network.messages.dto.MessageDto
import com.freelink.core.network.messages.dto.MessageEnvelopeDto
import com.freelink.core.network.messages.mapper.toDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorMessageApiClient(
    private val baseUrl: String = "http://10.0.2.2:8080"
) : MessageApiClient {
    private val client: HttpClient = defaultClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun fetchMessages(
        accessToken: String,
        chatId: String,
        limit: Int?
    ): AuthApiResult<List<Message>> {
        val response = client.get("$baseUrl/messages") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            parameter("chatId", chatId)
            if (limit != null) {
                parameter("limit", limit)
            }
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to fetch messages",
                statusCode = response.status.value
            )
        }

        val bodyText = response.body<String>()
        val items = json.parseToJsonElement(bodyText) as? JsonArray ?: JsonArray(emptyList())
        val messages = items.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            MessageDto(
                id = obj.stringOrEmpty("id"),
                chatId = obj.stringOrEmpty("chatId"),
                senderUserId = obj.stringOrEmpty("senderUserId"),
                body = obj.stringOrEmpty("body"),
                createdAtEpochMs = obj.longOrZero("createdAtEpochMs"),
                deliveryState = obj.stringOrEmpty("deliveryState"),
                envelope = obj.envelopeOrNull("envelope")
            )
        }.map { it.toDomain() }

        return AuthApiResult.Success(messages)
    }

    override suspend fun sendMessage(accessToken: String, chatId: String, body: String): AuthApiResult<Message> {
        val response = client.post("$baseUrl/messages") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                JsonObject(
                    mapOf(
                        "chatId" to JsonPrimitive(chatId),
                        "body" to JsonPrimitive(body)
                    )
                )
            )
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to send message",
                statusCode = response.status.value
            )
        }

        val bodyText = response.body<String>()
        val root = json.parseToJsonElement(bodyText) as? JsonObject
            ?: return AuthApiResult.Failure(message = "Malformed message payload", statusCode = response.status.value)

        val dto = MessageDto(
            id = root.stringOrEmpty("id"),
            chatId = root.stringOrEmpty("chatId"),
            senderUserId = root.stringOrEmpty("senderUserId"),
            body = root.stringOrEmpty("body"),
            createdAtEpochMs = root.longOrZero("createdAtEpochMs"),
            deliveryState = root.stringOrEmpty("deliveryState"),
            envelope = root.envelopeOrNull("envelope")
        )
        return AuthApiResult.Success(dto.toDomain())
    }

    private fun HttpStatusCode.isSuccess(): Boolean {
        return value in 200..299
    }

    private fun JsonObject.stringOrEmpty(key: String): String {
        val value = this[key] as? JsonPrimitive
        return value?.content ?: ""
    }

    private fun JsonObject.longOrZero(key: String): Long {
        val value = this[key] as? JsonPrimitive
        return value?.content?.toLongOrNull() ?: 0L
    }

    private fun JsonObject.envelopeOrNull(key: String): MessageEnvelopeDto? {
        val value = this[key] as? JsonObject ?: return null
        return MessageEnvelopeDto(
            version = value.stringOrEmpty("version").toIntOrNull() ?: 1,
            conversationId = value.stringOrEmpty("conversationId"),
            senderDeviceId = value.stringOrEmpty("senderDeviceId"),
            ciphertext = value.stringOrEmpty("ciphertext"),
            nonce = value.stringOrEmpty("nonce"),
            sentAt = value.stringOrEmpty("sentAt")
        )
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
