package com.freelink.core.network

import com.freelink.android.core.network.BuildConfig

object NetworkEndpoints {
    const val ApiBaseUrl: String = BuildConfig.FREELINK_API_BASE_URL
    const val ChatListWsUrl: String = BuildConfig.FREELINK_CHAT_LIST_WS_URL
    const val DirectChatWsUrl: String = BuildConfig.FREELINK_DIRECT_CHAT_WS_URL
}
