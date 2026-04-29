package com.freelink.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.messages.KtorMessageApiClient
import com.freelink.core.network.messages.ws.KtorDirectChatWsEventsClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.chat.data.DirectChatRepository
import com.freelink.feature.chat.ui.model.DirectMessageUiModel
import com.freelink.feature.chat.ui.model.ReplyTargetUiModel

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
    val repository = remember {
        DirectChatRepository(
            authRepository = authRepository,
            messageApiClient = KtorMessageApiClient(),
            directChatWsEventsClient = KtorDirectChatWsEventsClient()
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

    DirectChatScreen(
        state = state,
        onBack = onBack,
        onDraftChanged = viewModel::onDraftChanged,
        onSendMessage = viewModel::sendMessage,
        onRefresh = viewModel::refresh,
        onReplyRequested = viewModel::onReplyRequested,
        onReplyCancelled = viewModel::onReplyCancelled,
        onReactionRequested = viewModel::onReactionRequested
    )
}

@Composable
private fun DirectChatScreen(
    state: DirectChatUiState,
    onBack: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRefresh: () -> Unit,
    onReplyRequested: (String) -> Unit,
    onReplyCancelled: () -> Unit,
    onReactionRequested: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = onBack,
                label = { Text("Back") }
            )
            Text(
                text = state.chatTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            AssistChip(
                onClick = onRefresh,
                label = { Text(if (state.isRefreshing) "Refreshing..." else "Refresh") }
            )
        }

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
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(
                    item = message,
                    onReply = { onReplyRequested(message.id) },
                    onReaction = { emoji -> onReactionRequested(message.id, emoji) }
                )
            }
        }

        ReplyPreview(
            replyTarget = state.replyTarget,
            onCancelReply = onReplyCancelled
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = onDraftChanged,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Message") }
            )
            AssistChip(
                onClick = onSendMessage,
                enabled = state.draft.isNotBlank(),
                label = { Text("Send") }
            )
        }
    }
}

@Composable
private fun MessageBubble(
    item: DirectMessageUiModel,
    onReply: () -> Unit,
    onReaction: (String) -> Unit
) {
    val backgroundColor = if (item.isOutgoing) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val alignment = if (item.isOutgoing) Alignment.End else Alignment.Start
    val textAlign = if (item.isOutgoing) TextAlign.End else TextAlign.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(
                    color = backgroundColor,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (item.replyToSnippet != null) {
                Text(
                    text = "Reply: ${item.replyToSnippet}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = textAlign,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = textAlign
            )

            if (item.reactions.isNotEmpty()) {
                Text(
                    text = item.reactions.entries.joinToString("  ") { "${it.key} ${it.value}" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = textAlign
                )
            }

            Text(
                text = listOfNotNull(item.timeLabel, item.deliveryStateLabel).joinToString(" | "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = textAlign
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (item.isOutgoing) {
                    Arrangement.spacedBy(6.dp, Alignment.End)
                } else {
                    Arrangement.spacedBy(6.dp, Alignment.Start)
                }
            ) {
                AssistChip(
                    onClick = onReply,
                    label = { Text("Reply") }
                )
                AssistChip(
                    onClick = { onReaction("👍") },
                    label = { Text("👍") }
                )
                AssistChip(
                    onClick = { onReaction("❤️") },
                    label = { Text("❤️") }
                )
            }
        }
    }
}

@Composable
private fun ReplyPreview(
    replyTarget: ReplyTargetUiModel?,
    onCancelReply: () -> Unit
) {
    if (replyTarget == null) {
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Replying to: ${replyTarget.snippet}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        AssistChip(
            onClick = onCancelReply,
            label = { Text("Cancel") }
        )
    }
}
