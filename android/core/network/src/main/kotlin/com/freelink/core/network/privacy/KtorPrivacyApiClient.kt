package com.freelink.core.network.privacy

import com.freelink.core.model.domain.PrivacySettings
import com.freelink.core.network.auth.AuthApiResult
import com.freelink.core.network.privacy.dto.PrivacySettingsDto
import com.freelink.core.network.privacy.mapper.toDomain
import com.freelink.core.network.privacy.mapper.toDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorPrivacyApiClient(
    private val baseUrl: String = "http://10.0.2.2:8080"
) : PrivacyApiClient {
    private val client: HttpClient = defaultClient()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun getSettings(accessToken: String): AuthApiResult<PrivacySettings> {
        val response = client.get("$baseUrl/privacy/settings") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        return parsePrivacySettingsResponse(
            status = response.status,
            bodyText = response.body()
        )
    }

    override suspend fun updateSettings(
        accessToken: String,
        settings: PrivacySettings
    ): AuthApiResult<PrivacySettings> {
        val response = client.put("$baseUrl/privacy/settings") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(settings.toDto())
        }

        return parsePrivacySettingsResponse(
            status = response.status,
            bodyText = response.body()
        )
    }

    private fun parsePrivacySettingsResponse(
        status: HttpStatusCode,
        bodyText: String
    ): AuthApiResult<PrivacySettings> {
        if (!status.isSuccess()) {
            return AuthApiResult.Failure(
                message = extractErrorMessage(bodyText) ?: "Failed to sync privacy settings",
                statusCode = status.value
            )
        }

        val dto = runCatching {
            val jsonObject = json.parseToJsonElement(bodyText) as? JsonObject ?: return@runCatching null
            PrivacySettingsDto(
                hiddenModeEnabled = jsonObject.booleanOrFalse("hiddenModeEnabled"),
                biometricLockRequired = jsonObject.booleanOrFalse("biometricLockRequired"),
                disappearingMessagesEnabled = jsonObject.booleanOrFalse("disappearingMessagesEnabled"),
                linkPreviewEnabled = jsonObject.booleanOrFalse("linkPreviewEnabled"),
                whoCanMessageMe = jsonObject.stringOrEmpty("whoCanMessageMe")
            )
        }.getOrNull()

        if (dto == null) {
            return AuthApiResult.Failure(
                message = "Malformed privacy settings response",
                statusCode = status.value
            )
        }

        return AuthApiResult.Success(dto.toDomain())
    }

    private fun extractErrorMessage(bodyText: String): String? {
        return runCatching {
            val objectValue = json.parseToJsonElement(bodyText) as? JsonObject
            objectValue?.stringOrEmpty("message")
        }.getOrNull()
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
