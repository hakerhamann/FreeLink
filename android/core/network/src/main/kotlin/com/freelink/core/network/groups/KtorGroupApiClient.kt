package com.freelink.core.network.groups

import com.freelink.core.model.domain.GroupSummary
import com.freelink.core.network.auth.AuthApiResult
import com.freelink.core.network.groups.dto.GroupDto
import com.freelink.core.network.groups.mapper.toDomain
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

class KtorGroupApiClient(
    private val baseUrl: String = "http://10.0.2.2:8080"
) : GroupApiClient {
    private val client: HttpClient = defaultClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun fetchGroups(accessToken: String, query: String?): AuthApiResult<List<GroupSummary>> {
        val response = client.get("$baseUrl/groups") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            if (!query.isNullOrBlank()) {
                parameter("q", query.trim())
            }
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to fetch groups",
                statusCode = response.status.value
            )
        }

        val bodyText = response.body<String>()
        val items = json.parseToJsonElement(bodyText) as? JsonArray ?: JsonArray(emptyList())
        val groups = items.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            obj.toGroupDto()
        }.map { it.toDomain() }

        return AuthApiResult.Success(groups)
    }

    override suspend fun createGroup(
        accessToken: String,
        title: String,
        memberUserIds: List<String>
    ): AuthApiResult<GroupSummary> {
        val response = client.post("$baseUrl/groups") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                JsonObject(
                    mapOf(
                        "title" to JsonPrimitive(title),
                        "memberUserIds" to JsonArray(memberUserIds.map(::JsonPrimitive))
                    )
                )
            )
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to create group",
                statusCode = response.status.value
            )
        }

        val bodyText = response.body<String>()
        val dto = (json.parseToJsonElement(bodyText) as? JsonObject)?.toGroupDto()
            ?: return AuthApiResult.Failure(
                message = "Malformed group payload",
                statusCode = response.status.value
            )

        return AuthApiResult.Success(dto.toDomain())
    }

    private fun JsonObject.toGroupDto(): GroupDto {
        return GroupDto(
            id = stringOrEmpty("id"),
            title = stringOrEmpty("title"),
            membersCount = intOrZero("membersCount"),
            myRole = stringOrEmpty("myRole"),
            lastMessagePreview = stringOrEmpty("lastMessagePreview"),
            updatedAtEpochMs = longOrZero("updatedAtEpochMs")
        )
    }

    private fun HttpStatusCode.isSuccess(): Boolean {
        return value in 200..299
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
