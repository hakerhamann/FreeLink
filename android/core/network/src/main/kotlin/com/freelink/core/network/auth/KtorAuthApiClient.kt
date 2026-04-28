package com.freelink.core.network.auth

import com.freelink.core.model.domain.auth.AuthSession
import com.freelink.core.model.domain.auth.DeviceSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorAuthApiClient(
    private val baseUrl: String = "http://10.0.2.2:8080"
) : AuthApiClient {
    private val client: HttpClient = defaultClient()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun register(login: String, password: String, deviceName: String): AuthApiResult<AuthSession> {
        return postAuth("/auth/register", login, password, deviceName)
    }

    override suspend fun login(login: String, password: String, deviceName: String): AuthApiResult<AuthSession> {
        return postAuth("/auth/login", login, password, deviceName)
    }

    override suspend fun refresh(refreshToken: String): AuthApiResult<AuthSession> {
        val response = client.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("{" + "\"refreshToken\":\"${refreshToken.escapeJson()}\"" + "}")
        }

        return parseAuthResponse(response.status, response.body())
    }

    override suspend fun logout(refreshToken: String): AuthApiResult<Unit> {
        val response = client.post("$baseUrl/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody("{" + "\"refreshToken\":\"${refreshToken.escapeJson()}\"" + "}")
        }

        return if (response.status == HttpStatusCode.NoContent) {
            AuthApiResult.Success(Unit)
        } else {
            AuthApiResult.Failure(
                message = "Logout failed",
                statusCode = response.status.value
            )
        }
    }

    override suspend fun listDevices(accessToken: String): AuthApiResult<List<DeviceSession>> {
        val response = client.get("$baseUrl/devices") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Failed to fetch devices",
                statusCode = response.status.value
            )
        }

        val bodyText = response.body<String>()
        val devicesJson = json.parseToJsonElement(bodyText)
        val deviceArray = devicesJson as? JsonArray ?: return AuthApiResult.Success(emptyList())

        val devices = deviceArray.mapNotNull { element ->
            val item = element as? JsonObject ?: return@mapNotNull null
            DeviceSession(
                deviceId = item.stringOrEmpty("deviceId"),
                deviceName = item.stringOrEmpty("deviceName"),
                lastSeenAtIso = item.stringOrEmpty("lastSeenAtIso"),
                isCurrent = item.booleanOrFalse("isCurrent")
            )
        }

        return AuthApiResult.Success(devices)
    }

    override suspend fun revokeDevice(accessToken: String, deviceId: String): AuthApiResult<Unit> {
        val response = client.delete("$baseUrl/devices/$deviceId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        return if (response.status == HttpStatusCode.NoContent) {
            AuthApiResult.Success(Unit)
        } else {
            AuthApiResult.Failure(
                message = "Failed to revoke device",
                statusCode = response.status.value
            )
        }
    }

    private suspend fun postAuth(
        path: String,
        login: String,
        password: String,
        deviceName: String
    ): AuthApiResult<AuthSession> {
        val safeLogin = login.escapeJson()
        val safePassword = password.escapeJson()
        val safeDeviceName = deviceName.escapeJson()

        val response = client.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            setBody("{" + "\"login\":\"$safeLogin\",\"password\":\"$safePassword\",\"deviceName\":\"$safeDeviceName\"" + "}")
        }

        return parseAuthResponse(response.status, response.body())
    }

    private fun parseAuthResponse(status: HttpStatusCode, bodyText: String): AuthApiResult<AuthSession> {
        if (!status.isSuccess()) {
            return AuthApiResult.Failure(
                message = extractErrorMessage(bodyText) ?: "Authentication failed",
                statusCode = status.value
            )
        }

        val jsonObject = json.parseToJsonElement(bodyText) as? JsonObject
            ?: return AuthApiResult.Failure("Malformed auth response", status.value)

        return AuthApiResult.Success(
            AuthSession(
                userId = jsonObject.stringOrEmpty("userId"),
                deviceId = jsonObject.stringOrEmpty("deviceId"),
                accessToken = jsonObject.stringOrEmpty("accessToken"),
                refreshToken = jsonObject.stringOrEmpty("refreshToken"),
                expiresInSeconds = jsonObject.longOrZero("expiresInSeconds")
            )
        )
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

    private fun String.escapeJson(): String {
        return replace("\\", "\\\\").replace("\"", "\\\"")
    }

    private fun JsonObject.stringOrEmpty(key: String): String {
        val value = this[key] as? JsonPrimitive
        return value?.content ?: ""
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
