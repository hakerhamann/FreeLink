package com.freelink.core.network.messages

import com.freelink.core.network.NetworkEndpoints

import com.freelink.core.model.domain.MediaAttachment
import com.freelink.core.model.domain.Message
import com.freelink.core.model.domain.MessageEnvelope
import com.freelink.core.network.auth.AuthApiResult
import com.freelink.core.network.messages.dto.MessageAttachmentDto
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorMessageApiClient(
    private val baseUrl: String = NetworkEndpoints.ApiBaseUrl
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
            obj.toMessageDto()
        }.map { it.toDomain() }

        return AuthApiResult.Success(messages)
    }

    override suspend fun sendMessage(
        accessToken: String,
        chatId: String,
        body: String,
        attachment: MediaAttachment?,
        envelope: MessageEnvelope?,
        replyToMessageId: String?
    ): AuthApiResult<Message> {
        val payload = buildMap<String, JsonElement> {
            put("chatId", JsonPrimitive(chatId))
            put("body", JsonPrimitive(body))
            if (attachment != null) {
                put("attachment", attachment.toJsonObject())
            }
            if (envelope != null) {
                put("envelope", envelope.toJsonObject())
            }
            if (!replyToMessageId.isNullOrBlank()) {
                put("replyToMessageId", JsonPrimitive(replyToMessageId))
            }
        }
        val response = client.post("$baseUrl/messages") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(JsonObject(payload))
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to send message",
                statusCode = response.status.value
            )
        }

        val bodyText = response.body<String>()
        val dto = (json.parseToJsonElement(bodyText) as? JsonObject)?.toMessageDto()
            ?: return AuthApiResult.Failure(
                message = "Malformed message payload",
                statusCode = response.status.value
            )
        return AuthApiResult.Success(dto.toDomain())
    }

    override suspend fun setReaction(
        accessToken: String,
        chatId: String,
        messageId: String,
        emoji: String
    ): AuthApiResult<Message> {
        val response = client.post("$baseUrl/messages/$messageId/reactions") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                JsonObject(
                    mapOf(
                        "chatId" to JsonPrimitive(chatId),
                        "emoji" to JsonPrimitive(emoji)
                    )
                )
            )
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to set reaction",
                statusCode = response.status.value
            )
        }

        val bodyText = response.body<String>()
        val dto = (json.parseToJsonElement(bodyText) as? JsonObject)?.toMessageDto()
            ?: return AuthApiResult.Failure(
                message = "Malformed reaction payload",
                statusCode = response.status.value
            )
        return AuthApiResult.Success(dto.toDomain())
    }

    private fun HttpStatusCode.isSuccess(): Boolean {
        return value in 200..299
    }

    private fun JsonObject.toMessageDto(): MessageDto {
        return MessageDto(
            id = stringOrEmpty("id"),
            chatId = stringOrEmpty("chatId"),
            senderUserId = stringOrEmpty("senderUserId"),
            body = stringOrEmpty("body"),
            createdAtEpochMs = longOrZero("createdAtEpochMs"),
            expiresAtEpochMs = longOrNull("expiresAtEpochMs"),
            deliveryState = stringOrEmpty("deliveryState"),
            attachment = attachmentOrNull("attachment"),
            envelope = envelopeOrNull("envelope"),
            replyToMessageId = stringOrNull("replyToMessageId"),
            reactions = intMapOrEmpty("reactions")
        )
    }

    private fun JsonObject.stringOrEmpty(key: String): String {
        val value = this[key] as? JsonPrimitive
        return value?.content ?: ""
    }

    private fun JsonObject.stringOrNull(key: String): String? {
        val value = this[key] as? JsonPrimitive ?: return null
        return value.content.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.longOrZero(key: String): Long {
        val value = this[key] as? JsonPrimitive
        return value?.content?.toLongOrNull() ?: 0L
    }

    private fun JsonObject.longOrNull(key: String): Long? {
        val value = this[key] as? JsonPrimitive ?: return null
        return value.content.toLongOrNull()
    }

    private fun JsonObject.attachmentOrNull(key: String): MessageAttachmentDto? {
        val value = this[key] as? JsonObject ?: return null
        return MessageAttachmentDto(
            id = value.stringOrEmpty("id"),
            type = value.stringOrEmpty("type"),
            fileName = value.stringOrEmpty("fileName"),
            digestSha256 = value.stringOrEmpty("digestSha256"),
            byteSize = value.longOrZero("byteSize"),
            mimeType = value.stringOrEmpty("mimeType"),
            downloadUrl = value.stringOrEmpty("downloadUrl")
        )
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

    private fun JsonObject.intMapOrEmpty(key: String): Map<String, Int> {
        val raw = this[key] as? JsonObject ?: return emptyMap()
        return raw.entries
            .mapNotNull { (emoji, value) ->
                val count = (value as? JsonPrimitive)?.content?.toIntOrNull() ?: return@mapNotNull null
                if (count > 0) emoji to count else null
            }
            .toMap(linkedMapOf())
    }

    private fun MessageEnvelope.toJsonObject(): JsonObject {
        return JsonObject(
            mapOf(
                "version" to JsonPrimitive(version),
                "conversationId" to JsonPrimitive(conversationId),
                "senderDeviceId" to JsonPrimitive(senderDeviceId),
                "ciphertext" to JsonPrimitive(ciphertextBase64),
                "nonce" to JsonPrimitive(nonceBase64),
                "sentAt" to JsonPrimitive(sentAtIso)
            )
        )
    }

    private fun MediaAttachment.toJsonObject(): JsonObject {
        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(id),
                "type" to JsonPrimitive(type.name),
                "fileName" to JsonPrimitive(fileName),
                "digestSha256" to JsonPrimitive(digestSha256),
                "byteSize" to JsonPrimitive(byteSize),
                "mimeType" to JsonPrimitive(mimeType),
                "downloadUrl" to JsonPrimitive(downloadUrl)
            )
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
