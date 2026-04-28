package com.freelink.core.network.people

import com.freelink.core.model.domain.User
import com.freelink.core.network.auth.AuthApiResult
import com.freelink.core.network.people.dto.PersonSummaryDto
import com.freelink.core.network.people.mapper.toDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorPeopleApiClient(
    private val baseUrl: String = "http://10.0.2.2:8080"
) : PeopleApiClient {
    private val client: HttpClient = defaultClient()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun fetchPeople(accessToken: String): AuthApiResult<List<User>> {
        val response = client.get("$baseUrl/people") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to fetch people",
                statusCode = response.status.value
            )
        }

        val bodyText = response.body<String>()
        val items = json.parseToJsonElement(bodyText) as? JsonArray ?: JsonArray(emptyList())
        val people = items.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            PersonSummaryDto(
                id = obj.stringOrEmpty("id"),
                login = obj.stringOrEmpty("login"),
                displayName = obj.stringOrEmpty("displayName"),
                isOnline = obj.booleanOrFalse("isOnline")
            )
        }.map { it.toDomain() }

        return AuthApiResult.Success(people)
    }

    private fun HttpStatusCode.isSuccess(): Boolean {
        return value in 200..299
    }

    private fun JsonObject.stringOrEmpty(key: String): String {
        val value = this[key] as? JsonPrimitive
        return value?.content ?: ""
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
