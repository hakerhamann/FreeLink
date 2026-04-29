package com.freelink.feature.profile.ui

import com.freelink.feature.profile.ui.model.ProfilePhotoUiModel
import com.freelink.feature.profile.ui.model.ProfilePinnedMessageUiModel
import com.freelink.feature.profile.ui.model.ProfileSharedChatUiModel
import com.freelink.feature.profile.ui.model.ProfileTrustedContactUiModel

internal data class ProfileSections(
    val title: String,
    val subtitle: String,
    val sharedChats: List<ProfileSharedChatUiModel>,
    val sharedPhotos: List<ProfilePhotoUiModel>,
    val pinnedMessages: List<ProfilePinnedMessageUiModel>,
    val trustedContacts: List<ProfileTrustedContactUiModel>
)

internal fun buildProfileSections(userId: String?): ProfileSections {
    val safeUserId = userId?.trim().orEmpty()
    val title = if (safeUserId.isBlank()) {
        "Protected profile"
    } else {
        "User ${safeUserId.takeLast(8)}"
    }

    val subtitle = if (safeUserId.isBlank()) {
        "Shared spaces, photos and trusted contacts appear after sign in."
    } else {
        "Private context for your closest chats and protected history."
    }

    return ProfileSections(
        title = title,
        subtitle = subtitle,
        sharedChats = listOf(
            ProfileSharedChatUiModel(
                id = "family",
                title = "Family",
                membersLabel = "6 members",
                accentLabel = "Warm circle"
            ),
            ProfileSharedChatUiModel(
                id = "travels",
                title = "Travels",
                membersLabel = "5 members",
                accentLabel = "Shared memories"
            ),
            ProfileSharedChatUiModel(
                id = "weekend",
                title = "Weekend plans",
                membersLabel = "4 members",
                accentLabel = "Trusted crew"
            )
        ),
        sharedPhotos = listOf(
            ProfilePhotoUiModel(id = "photo-1", caption = "Lake sunset"),
            ProfilePhotoUiModel(id = "photo-2", caption = "City lights"),
            ProfilePhotoUiModel(id = "photo-3", caption = "Coffee morning"),
            ProfilePhotoUiModel(id = "photo-4", caption = "Skyline dusk")
        ),
        pinnedMessages = listOf(
            ProfilePinnedMessageUiModel(
                id = "pin-1",
                title = "Playlist for you",
                subtitle = "Listen when you want a quiet vibe.",
                dateLabel = "12 May"
            ),
            ProfilePinnedMessageUiModel(
                id = "pin-2",
                title = "Shared weekend checklist",
                subtitle = "Blankets, projector and snacks are all set.",
                dateLabel = "03 May"
            )
        ),
        trustedContacts = listOf(
            ProfileTrustedContactUiModel(
                id = "lera",
                name = "Lera",
                status = "Closest contact"
            ),
            ProfileTrustedContactUiModel(
                id = "artem",
                name = "Artem",
                status = "Shared groups and media"
            ),
            ProfileTrustedContactUiModel(
                id = "masha",
                name = "Masha",
                status = "Priority access"
            )
        )
    )
}
