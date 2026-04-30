package com.freelink.baselineprofile

data class BaselineProfileTarget(
    val scenarioId: String,
    val routeHint: String,
    val warmupActions: List<String>
)

object BaselineProfileTargets {
    val all: List<BaselineProfileTarget> = listOf(
        BaselineProfileTarget(
            scenarioId = "startup",
            routeHint = "auth-or-chats",
            warmupActions = listOf("launch-app", "render-root-nav")
        ),
        BaselineProfileTarget(
            scenarioId = "chat-list",
            routeHint = "chats",
            warmupActions = listOf("open-chats-tab", "perform-search", "toggle-unread-filter")
        ),
        BaselineProfileTarget(
            scenarioId = "open-chat",
            routeHint = "chat/{chatId}",
            warmupActions = listOf("open-direct-chat", "send-text", "react-to-message")
        ),
        BaselineProfileTarget(
            scenarioId = "media",
            routeHint = "chat/{chatId}",
            warmupActions = listOf("attach-photo", "attach-video", "open-attachment")
        ),
        BaselineProfileTarget(
            scenarioId = "voice",
            routeHint = "chat/{chatId}",
            warmupActions = listOf("attach-voice", "render-waveform")
        )
    )
}
