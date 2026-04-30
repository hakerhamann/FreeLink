plugins {
    id("freelink.android.library")
}

android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val apiBaseUrl = providers.gradleProperty("freelink.apiBaseUrl")
            .orElse("http://10.0.2.2:8080")
            .get()
        val chatListWsUrl = providers.gradleProperty("freelink.chatListWsUrl")
            .orElse("ws://10.0.2.2:8081/ws/chats")
            .get()
        val directChatWsUrl = providers.gradleProperty("freelink.directChatWsUrl")
            .orElse("ws://10.0.2.2:8081/ws/messages")
            .get()

        buildConfigField("String", "FREELINK_API_BASE_URL", apiBaseUrl.toBuildConfigString())
        buildConfigField("String", "FREELINK_CHAT_LIST_WS_URL", chatListWsUrl.toBuildConfigString())
        buildConfigField("String", "FREELINK_DIRECT_CHAT_WS_URL", directChatWsUrl.toBuildConfigString())
    }
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":android:core:model"))
    testImplementation(libs.junit4)
}

private fun String.toBuildConfigString(): String {
    return "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

