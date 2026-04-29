package com.freelink.feature.chatlist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelink.feature.chatlist.ui.model.ChatListItemUiModel
import com.freelink.feature.chatlist.ui.model.ChatListPersonUiModel

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun ChatListItem(
    item: ChatListItemUiModel,
    onClick: () -> Unit,
    onArchive: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ChatListItemHeader(item = item)

        Text(
            text = item.lastMessagePreview,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            AssistChip(
                onClick = onArchive,
                label = { Text("Archive") }
            )
        }
    }
}

@Composable
fun PersonSearchItem(item: ChatListPersonUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.status,
                style = MaterialTheme.typography.labelSmall,
                color = if (item.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }

        Text(
            text = item.login,
            style = MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider()
    }
}

@Composable
private fun ChatListItemHeader(item: ChatListItemUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.chatTypeLabel,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.unreadBadge != null) {
                Text(
                    text = item.unreadBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = item.updatedAtLabel,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
