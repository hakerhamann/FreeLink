package com.freelink.feature.profile.ui

import com.freelink.feature.profile.ui.model.ProfilePinnedMessageUiModel
import com.freelink.feature.profile.ui.model.ProfilePhotoUiModel
import com.freelink.feature.profile.ui.model.ProfileSharedChatUiModel
import com.freelink.feature.profile.ui.model.ProfileTrustedContactUiModel

data class ProfileUiState(
    val userId: String? = null,
    val deviceId: String? = null,
    val isSessionAvailable: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val title: String = "Защищённый профиль",
    val subtitle: String = "Здесь появятся личные чаты и приватный контекст.",
    val sharedChats: List<ProfileSharedChatUiModel> = emptyList(),
    val sharedPhotos: List<ProfilePhotoUiModel> = emptyList(),
    val pinnedMessages: List<ProfilePinnedMessageUiModel> = emptyList(),
    val trustedContacts: List<ProfileTrustedContactUiModel> = emptyList(),
    val errorMessage: String? = null
)
