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
    val title = if (userId.isNullOrBlank()) "Защищённый профиль" else "Мой профиль"
    val subtitle = if (userId.isNullOrBlank()) {
        "Чаты, фото и доверенные контакты появятся после входа."
    } else {
        "Личный контекст для близких чатов и защищённой истории."
    }

    return ProfileSections(
        title = title,
        subtitle = subtitle,
        sharedChats = listOf(
            ProfileSharedChatUiModel("family", "Семья", "6 участников", "Тёплый круг"),
            ProfileSharedChatUiModel("travels", "Путешествия", "5 участников", "Общие воспоминания"),
            ProfileSharedChatUiModel("weekend", "Планы на выходные", "4 участника", "Свои люди")
        ),
        sharedPhotos = listOf(
            ProfilePhotoUiModel("photo-1", "Закат у озера"),
            ProfilePhotoUiModel("photo-2", "Огни города"),
            ProfilePhotoUiModel("photo-3", "Утренний кофе"),
            ProfilePhotoUiModel("photo-4", "Вечерний skyline")
        ),
        pinnedMessages = listOf(
            ProfilePinnedMessageUiModel("pin-1", "Плейлист для тебя", "Включай, когда нужен тихий вайб.", "12 мая"),
            ProfilePinnedMessageUiModel("pin-2", "Список на выходные", "Пледы, проектор и снеки уже готовы.", "03 мая")
        ),
        trustedContacts = listOf(
            ProfileTrustedContactUiModel("lera", "Лера", "Самый близкий контакт"),
            ProfileTrustedContactUiModel("artem", "Артём", "Общие группы и медиа"),
            ProfileTrustedContactUiModel("masha", "Маша", "Приоритетный доступ")
        )
    )
}
