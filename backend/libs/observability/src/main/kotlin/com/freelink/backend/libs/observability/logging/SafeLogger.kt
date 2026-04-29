package com.freelink.backend.libs.observability.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SafeLogger(
    private val delegate: Logger
) {
    fun info(message: String) {
        delegate.info(SecretRedactor.redact(message))
    }

    fun warn(message: String) {
        delegate.warn(SecretRedactor.redact(message))
    }

    fun error(message: String, throwable: Throwable? = null) {
        delegate.error(SecretRedactor.redact(message), throwable)
    }
}

object SafeLoggers {
    fun forName(name: String): SafeLogger {
        return SafeLogger(LoggerFactory.getLogger(name))
    }
}
