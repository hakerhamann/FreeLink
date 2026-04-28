package com.freelink.backend.libs.auth.service

import com.freelink.backend.libs.auth.domain.AuthTokens
import com.freelink.backend.libs.auth.domain.DeviceSession
import de.mkammerer.argon2.Argon2Factory
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryAuthService : AuthService {
    private val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
    private val random = SecureRandom()

    private val usersByLogin = ConcurrentHashMap<String, UserRecord>()
    private val sessionsByRefresh = ConcurrentHashMap<String, SessionRecord>()
    private val sessionsByAccess = ConcurrentHashMap<String, SessionRecord>()
    private val failedAttempts = ConcurrentHashMap<String, FailedAttemptRecord>()

    override fun register(command: RegisterCommand): AuthTokens? {
        val normalizedLogin = command.login.trim().lowercase()
        if (!isCredentialInputValid(normalizedLogin, command.password)) {
            return null
        }

        val existing = usersByLogin[normalizedLogin]
        if (existing != null) {
            return null
        }

        val userId = UUID.randomUUID().toString()
        val passwordHash = argon2.hash(3, 1 shl 16, 1, command.password.toCharArray())
        val userRecord = UserRecord(
            userId = userId,
            login = normalizedLogin,
            passwordHash = passwordHash,
            deviceIds = LinkedHashSet()
        )

        usersByLogin[normalizedLogin] = userRecord
        return createSession(userRecord, command.deviceName)
    }

    override fun login(command: LoginCommand): AuthTokens? {
        val normalizedLogin = command.login.trim().lowercase()
        if (!isCredentialInputValid(normalizedLogin, command.password)) {
            return null
        }

        if (isTemporarilyBlocked(normalizedLogin)) {
            return null
        }

        val userRecord = usersByLogin[normalizedLogin]
        if (userRecord == null || !argon2.verify(userRecord.passwordHash, command.password.toCharArray())) {
            registerFailedAttempt(normalizedLogin)
            return null
        }

        clearFailedAttempts(normalizedLogin)
        return createSession(userRecord, command.deviceName)
    }

    override fun refresh(refreshToken: String): AuthTokens? {
        val session = sessionsByRefresh.remove(refreshToken) ?: return null
        sessionsByAccess.remove(session.accessToken)

        val userRecord = usersByLogin.values.firstOrNull { it.userId == session.userId } ?: return null
        return createSession(userRecord, session.deviceName, existingDeviceId = session.deviceId)
    }

    override fun logout(refreshToken: String): Boolean {
        val session = sessionsByRefresh.remove(refreshToken) ?: return false
        sessionsByAccess.remove(session.accessToken)
        return true
    }

    override fun listDevicesByAccessToken(accessToken: String): List<DeviceSession>? {
        val currentSession = sessionsByAccess[accessToken] ?: return null
        return sessionsByRefresh.values
            .filter { it.userId == currentSession.userId }
            .sortedByDescending { it.lastSeenAtIso }
            .map {
                DeviceSession(
                    deviceId = it.deviceId,
                    deviceName = it.deviceName,
                    lastSeenAtIso = it.lastSeenAtIso,
                    isCurrent = it.accessToken == accessToken
                )
            }
    }

    override fun revokeDeviceByAccessToken(accessToken: String, deviceId: String): Boolean {
        val currentSession = sessionsByAccess[accessToken] ?: return false
        val toRemove = sessionsByRefresh.values.filter {
            it.userId == currentSession.userId && it.deviceId == deviceId
        }

        if (toRemove.isEmpty()) {
            return false
        }

        toRemove.forEach { session ->
            sessionsByRefresh.remove(session.refreshToken)
            sessionsByAccess.remove(session.accessToken)
        }

        return true
    }

    override fun resolveUserIdByAccessToken(accessToken: String): String? {
        return sessionsByAccess[accessToken]?.userId
    }

    private fun isCredentialInputValid(login: String, password: String): Boolean {
        return login.length in 3..64 && password.length in 8..128
    }

    private fun createSession(
        userRecord: UserRecord,
        deviceName: String,
        existingDeviceId: String? = null
    ): AuthTokens {
        val deviceId = existingDeviceId ?: UUID.randomUUID().toString()
        userRecord.deviceIds += deviceId

        val accessToken = randomToken(bytes = 32)
        val refreshToken = randomToken(bytes = 48)
        val issuedAt = Instant.now().toString()

        val sessionRecord = SessionRecord(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userRecord.userId,
            deviceId = deviceId,
            deviceName = deviceName.ifBlank { "Android Device" },
            lastSeenAtIso = issuedAt
        )

        sessionsByRefresh[refreshToken] = sessionRecord
        sessionsByAccess[accessToken] = sessionRecord

        return AuthTokens(
            userId = userRecord.userId,
            deviceId = deviceId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInSeconds = 900
        )
    }

    private fun randomToken(bytes: Int): String {
        val value = ByteArray(bytes)
        random.nextBytes(value)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value)
    }

    private fun isTemporarilyBlocked(login: String): Boolean {
        val record = failedAttempts[login] ?: return false
        return record.failCount >= 5 && Instant.now().isBefore(record.blockedUntil)
    }

    private fun registerFailedAttempt(login: String) {
        val existing = failedAttempts[login]
        val now = Instant.now()
        val next = if (existing == null || now.isAfter(existing.blockedUntil)) {
            FailedAttemptRecord(failCount = 1, blockedUntil = now.plusSeconds(30))
        } else {
            FailedAttemptRecord(failCount = existing.failCount + 1, blockedUntil = now.plusSeconds(30))
        }
        failedAttempts[login] = next
    }

    private fun clearFailedAttempts(login: String) {
        failedAttempts.remove(login)
    }

    private data class UserRecord(
        val userId: String,
        val login: String,
        val passwordHash: String,
        val deviceIds: MutableSet<String>
    )

    private data class SessionRecord(
        val accessToken: String,
        val refreshToken: String,
        val userId: String,
        val deviceId: String,
        val deviceName: String,
        val lastSeenAtIso: String
    )

    private data class FailedAttemptRecord(
        val failCount: Int,
        val blockedUntil: Instant
    )
}
