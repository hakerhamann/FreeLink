package com.freelink.feature.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

@Composable
fun DirectChatScreen(
    state: DirectChatUiState,
    onBack: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachmentActionSelected: (String) -> Unit,
    onRemoveAttachment: () -> Unit,
    onRefresh: () -> Unit,
    onReplyRequested: (String) -> Unit,
    onReplyCancelled: () -> Unit,
    onReactionRequested: (String, String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChatHeader(
            title = state.chatTitle,
            isRefreshing = state.isRefreshing,
            onBack = onBack,
            onRefresh = onRefresh
        )

        HorizontalDivider()

        if (state.isPeerTyping) {
            Text(
                text = "Peer is typing...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(
                    item = message,
                    onReply = { onReplyRequested(message.id) },
                    onReaction = { emoji -> onReactionRequested(message.id, emoji) },
                    onOpenAttachment = { downloadUrl -> uriHandler.openUri(downloadUrl) }
                )
            }
        }

        ReplyPreview(replyTarget = state.replyTarget, onCancelReply = onReplyCancelled)
        PendingAttachmentPreview(
            actions = state.attachmentTrayActions,
            attachment = state.pendingAttachment,
            isAttaching = state.isAttaching,
            onAttachmentActionSelected = onAttachmentActionSelected,
            onRemoveAttachment = onRemoveAttachment
        )

        ComposerRow(
            draft = state.draft,
            canSend = state.draft.isNotBlank() || state.pendingAttachment != null,
            onDraftChanged = onDraftChanged,
            onSendMessage = onSendMessage
        )
    }
}
