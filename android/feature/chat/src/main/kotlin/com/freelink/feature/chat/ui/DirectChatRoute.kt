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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.feature.chat.ui.model.DirectMessageUiModel

@Composable
fun DirectChatRoute(
    chatId: String,
    chatTitle: String,
    onBack: () -> Unit
) {
    val viewModel: DirectChatViewModel = viewModel(
        key = "direct-chat-$chatId",
        factory = DirectChatViewModel.factory(chatId = chatId, chatTitle = chatTitle)
    )
    val state by viewModel.uiState.collectAsState()

    DirectChatScreen(
        state = state,
        onBack = onBack,
        onDraftChanged = viewModel::onDraftChanged,
        onSendMessage = viewModel::sendMessage
    )
}

@Composable
private fun DirectChatScreen(
    state: DirectChatUiState,
    onBack: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSendMessage: () -> Unit
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
            Text(
                text = if (state.isPeerTyping) "typing..." else "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(item = message)
            }
        }

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
                label = { Text("Send") }
            )
        }
    }
}

@Composable
private fun MessageBubble(item: DirectMessageUiModel) {
    val backgroundColor = if (item.isOutgoing) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val alignment = if (item.isOutgoing) Alignment.End else Alignment.Start
    val textAlign = if (item.isOutgoing) TextAlign.End else TextAlign.Start

    Column(
        modifier = Modifier
            .fillMaxWidth(),
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
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = textAlign
            )
            Text(
                text = listOfNotNull(item.timeLabel, item.deliveryStateLabel).joinToString(" • "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = textAlign
            )
        }
    }
}
