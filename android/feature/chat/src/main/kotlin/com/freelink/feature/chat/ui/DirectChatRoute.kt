package com.freelink.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.datastore.privacy.PrivacySettingsStore
import com.freelink.core.encryption.TransportMessageEnvelopeFactory
import com.freelink.core.model.domain.PrivacySettings
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.media.KtorMediaApiClient
import com.freelink.core.network.messages.KtorMessageApiClient
import com.freelink.core.network.messages.ws.KtorDirectChatWsEventsClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.chat.data.DirectChatRepository

@Composable
fun DirectChatRoute(
    chatId: String,
    chatTitle: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val authRepository = remember {
        AuthRepository(
            apiClient = KtorAuthApiClient(),
            sessionStore = AuthSessionStore.create(context)
        )
    }
    val privacySettingsStore = remember {
        PrivacySettingsStore.create(context)
    }
    val repository = remember {
        DirectChatRepository(
            authRepository = authRepository,
            mediaApiClient = KtorMediaApiClient(),
            messageApiClient = KtorMessageApiClient(),
            directChatWsEventsClient = KtorDirectChatWsEventsClient(),
            messageEnvelopeFactory = TransportMessageEnvelopeFactory()
        )
    }
    val viewModel: DirectChatViewModel = viewModel(
        key = "direct-chat-$chatId",
        factory = DirectChatViewModel.factory(
            chatId = chatId,
            chatTitle = chatTitle,
            repository = repository
        )
    )
    val state by viewModel.uiState.collectAsState()
    val privacySettings by privacySettingsStore.settingsFlow.collectAsState(
        initial = PrivacySettings(
            hiddenModeEnabled = false,
            biometricLockRequired = false,
            disappearingMessagesEnabled = false,
            linkPreviewEnabled = true,
            whoCanMessageMe = "trusted_contacts"
        )
    )

    DirectChatScreen(
        state = state.copy(linkPreviewEnabled = privacySettings.linkPreviewEnabled),
        onBack = onBack,
        onDraftChanged = viewModel::onDraftChanged,
        onSendMessage = viewModel::sendMessage,
        onAttachmentActionSelected = viewModel::onAttachmentActionSelected,
        onRemoveAttachment = viewModel::removePendingAttachment,
        onRefresh = viewModel::refresh,
        onReplyRequested = viewModel::onReplyRequested,
        onReplyCancelled = viewModel::onReplyCancelled,
        onReactionRequested = viewModel::onReactionRequested
    )
}
