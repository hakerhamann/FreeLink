package com.freelink.benchmark

data class PerformanceScenario(
    val id: String,
    val displayName: String,
    val budgetMs: Long,
    val criticalPath: List<String>
)

object PerformanceScenarios {
    val all: List<PerformanceScenario> = listOf(
        PerformanceScenario(
            id = "startup",
            displayName = "Cold startup to auth/chat shell",
            budgetMs = 1_500,
            criticalPath = listOf("launch", "session-read", "first-compose-frame")
        ),
        PerformanceScenario(
            id = "chat-list",
            displayName = "Open and sync chat list",
            budgetMs = 900,
            criticalPath = listOf("room-read", "api-sync", "search-filter")
        ),
        PerformanceScenario(
            id = "open-chat",
            displayName = "Open direct chat history",
            budgetMs = 900,
            criticalPath = listOf("message-api", "ui-map", "first-message-frame")
        ),
        PerformanceScenario(
            id = "media",
            displayName = "Attach and preview media",
            budgetMs = 1_200,
            criticalPath = listOf("digest", "media-init", "media-complete", "bubble-preview")
        ),
        PerformanceScenario(
            id = "voice",
            displayName = "Render voice message bubble",
            budgetMs = 700,
            criticalPath = listOf("voice-attachment-map", "waveform-build", "bubble-render")
        )
    )
}
