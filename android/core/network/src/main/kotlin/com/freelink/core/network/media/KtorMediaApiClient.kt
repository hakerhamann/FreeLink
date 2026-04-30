package com.freelink.core.network.media

import com.freelink.core.network.NetworkEndpoints

import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.model.domain.MediaUploadSession
import com.freelink.core.model.domain.UploadedMedia
import com.freelink.core.network.auth.AuthApiResult
import com.freelink.core.network.media.dto.MediaUploadSessionDto
import com.freelink.core.network.media.dto.UploadedMediaDto
import com.freelink.core.network.media.mapper.toDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorMediaApiClient(
    private val baseUrl: String = NetworkEndpoints.ApiBaseUrl
) : MediaApiClient {
    private val client: HttpClient = defaultClient()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun initUpload(
        accessToken: String,
        fileName: String,
        mimeType: String,
        digestSha256: String,
        byteSize: Long,
        attachmentType: AttachmentType
    ): AuthApiResult<MediaUploadSession> {
        val response = client.post("$baseUrl/media/init") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                JsonObject(
                    mapOf(
                        "fileName" to JsonPrimitive(fileName),
                        "mimeType" to JsonPrimitive(mimeType),
                        "digestSha256" to JsonPrimitive(digestSha256),
                        "byteSize" to JsonPrimitive(byteSize),
                        "attachmentType" to JsonPrimitive(attachmentType.name)
                    )
                )
            )
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Не удалось подготовить загрузку файла.",
                statusCode = response.status.value
            )
        }

        val dto = (json.parseToJsonElement(response.body<String>()) as? JsonObject)?.toSessionDto()
            ?: return AuthApiResult.Failure(
                message = "Сервер вернул некорректные данные загрузки файла.",
                statusCode = response.status.value
            )
        return AuthApiResult.Success(dto.toDomain())
    }

    override suspend fun completeUpload(
        accessToken: String,
        uploadId: String
    ): AuthApiResult<UploadedMedia> {
        val response = client.post("$baseUrl/media/complete") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                JsonObject(
                    mapOf(
                        "uploadId" to JsonPrimitive(uploadId)
                    )
                )
            )
        }

        if (!response.status.isSuccess()) {
            return AuthApiResult.Failure(
                message = "Не удалось завершить загрузку файла.",
                statusCode = response.status.value
            )
        }

        val dto = (json.parseToJsonElement(response.body<String>()) as? JsonObject)?.toUploadedMediaDto()
            ?: return AuthApiResult.Failure(
                message = "Сервер вернул некорректные данные файла.",
                statusCode = response.status.value
            )
        return AuthApiResult.Success(dto.toDomain())
    }

    private fun JsonObject.toSessionDto(): MediaUploadSessionDto {
        return MediaUploadSessionDto(
            uploadId = stringOrEmpty("uploadId"),
            attachmentId = stringOrNull("attachmentId"),
            blobKey = stringOrEmpty("blobKey"),
            uploadUrl = stringOrEmpty("uploadUrl"),
            alreadyExists = booleanOrFalse("alreadyExists")
        )
    }

    private fun JsonObject.toUploadedMediaDto(): UploadedMediaDto {
        return UploadedMediaDto(
            attachmentId = stringOrEmpty("attachmentId"),
            digestSha256 = stringOrEmpty("digestSha256"),
            byteSize = longOrZero("byteSize"),
            mimeType = stringOrEmpty("mimeType"),
            fileName = stringOrEmpty("fileName"),
            attachmentType = stringOrEmpty("attachmentType"),
            blobKey = stringOrEmpty("blobKey"),
            downloadUrl = stringOrEmpty("downloadUrl")
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

    private fun JsonObject.booleanOrFalse(key: String): Boolean {
        val value = this[key] as? JsonPrimitive
        return value?.content?.toBooleanStrictOrNull() ?: false
    }

    private fun JsonObject.longOrZero(key: String): Long {
        val value = this[key] as? JsonPrimitive
        return value?.content?.toLongOrNull() ?: 0L
    }

    private fun HttpStatusCode.isSuccess(): Boolean {
        return value in 200..299
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
