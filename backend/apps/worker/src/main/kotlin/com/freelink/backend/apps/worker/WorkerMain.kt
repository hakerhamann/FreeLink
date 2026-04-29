package com.freelink.backend.apps.worker

import com.freelink.backend.libs.observability.logging.SafeLoggers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

private val logger = SafeLoggers.forName("freelink.worker")

fun main() = runBlocking {
    while (true) {
        logger.info("worker heartbeat")
        delay(10_000)
    }
}
