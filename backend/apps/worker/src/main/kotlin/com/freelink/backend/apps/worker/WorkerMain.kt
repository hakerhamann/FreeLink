package com.freelink.backend.apps.worker

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    while (true) {
        println("worker heartbeat")
        delay(10_000)
    }
}
