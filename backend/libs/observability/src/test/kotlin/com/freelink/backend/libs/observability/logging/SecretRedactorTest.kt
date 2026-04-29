package com.freelink.backend.libs.observability.logging

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretRedactorTest {
    @Test
    fun redactsBearerTokensAndCredentials() {
        val raw = """
            Authorization: Bearer access-secret
            {"accessToken":"access-json","refreshToken":"refresh-json","password":"plain","secret":"value"}
            accessToken=query-token refreshToken=query-refresh password=query-password secret=query-secret
        """.trimIndent()

        val redacted = SecretRedactor.redact(raw)

        assertFalse(redacted.contains("access-secret"))
        assertFalse(redacted.contains("access-json"))
        assertFalse(redacted.contains("refresh-json"))
        assertFalse(redacted.contains("plain"))
        assertFalse(redacted.contains("query-token"))
        assertTrue(redacted.contains("[REDACTED]"))
    }
}
