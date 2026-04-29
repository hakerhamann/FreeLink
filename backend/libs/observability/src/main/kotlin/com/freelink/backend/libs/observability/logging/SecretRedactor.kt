package com.freelink.backend.libs.observability.logging

object SecretRedactor {
    private val rules = listOf(
        Regex("""(?i)(authorization\s*:\s*bearer\s+)[^\s"]+""") to "$1[REDACTED]",
        Regex("""(?i)("?(accessToken|refreshToken|password|secret)"?\s*[:=]\s*")([^"]*)(")""") to "$1[REDACTED]$4",
        Regex("""(?i)\b(accessToken|refreshToken|password|secret)=([^\s,}]+)""") to "$1=[REDACTED]"
    )

    fun redact(message: String): String {
        return rules.fold(message) { redacted, (pattern, replacement) ->
            pattern.replace(redacted, replacement)
        }
    }
}
