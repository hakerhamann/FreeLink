package com.freelink.feature.archive.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelink.core.model.domain.Chat

@Composable
internal fun ArchiveScreen(
    state: ArchiveUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRestoreChat: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AssistChip(onClick = onBack, label = { Text("Back") })
        Text(
            text = "Archive",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Protected history hidden from the main chat list.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        if (state.isLoading) {
            CircularProgressIndicator()
        }
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.chats, key = { it.id }) { chat ->
                ArchivedChatCard(
                    chat = chat,
                    isRestoring = state.restoringChatId == chat.id,
                    onRestoreChat = onRestoreChat
                )
            }
        }

        if (!state.isLoading && state.chats.isEmpty()) {
            Text(
                text = "No archived chats yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedButton(
            onClick = onRefresh,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh archive")
        }
    }
}

@Composable
private fun ArchivedChatCard(
    chat: Chat,
    isRestoring: Boolean,
    onRestoreChat: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = chat.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = chat.lastMessagePreview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            AssistChip(
                onClick = { onRestoreChat(chat.id) },
                enabled = !isRestoring,
                label = { Text(if (isRestoring) "Restoring..." else "Restore") }
            )
        }
    }
}
