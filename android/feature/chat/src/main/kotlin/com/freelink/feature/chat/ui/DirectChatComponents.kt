package com.freelink.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freelink.feature.chat.ui.model.AttachmentTrayActionUiModel
import com.freelink.feature.chat.ui.model.DirectMessageAttachmentUiModel
import com.freelink.feature.chat.ui.model.DirectMessageUiModel
import com.freelink.feature.chat.ui.model.ReplyTargetUiModel

@Composable
fun ChatHeader(
    title: String,
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(onClick = onBack, label = { Text("Back") })
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        AssistChip(
            onClick = onRefresh,
            label = { Text(if (isRefreshing) "Refreshing..." else "Refresh") }
        )
    }
}

@Composable
fun ComposerRow(
    draft: String,
    canSend: Boolean,
    onDraftChanged: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChanged,
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("Message") }
        )
        AssistChip(
            onClick = onSendMessage,
            enabled = canSend,
            label = { Text("Send") }
        )
    }
}

@Composable
fun MessageBubble(
    item: DirectMessageUiModel,
    onReply: () -> Unit,
    onReaction: (String) -> Unit,
    onOpenAttachment: (String) -> Unit
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
                .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
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
            if (item.attachment != null) {
                AttachmentCard(
                    attachment = item.attachment,
                    textAlign = textAlign,
                    onOpenAttachment = onOpenAttachment
                )
            }
            if (item.text.isNotBlank()) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = textAlign
                )
            }
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
                AssistChip(onClick = onReply, label = { Text("Reply") })
                AssistChip(onClick = { onReaction("\uD83D\uDC4D") }, label = { Text("\uD83D\uDC4D") })
                AssistChip(onClick = { onReaction("\u2764\uFE0F") }, label = { Text("\u2764\uFE0F") })
            }
        }
    }
}

@Composable
fun ReplyPreview(
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
        AssistChip(onClick = onCancelReply, label = { Text("Cancel") })
    }
}

@Composable
fun PendingAttachmentPreview(
    actions: List<AttachmentTrayActionUiModel>,
    attachment: DirectMessageAttachmentUiModel?,
    isAttaching: Boolean,
    onAttachmentActionSelected: (String) -> Unit,
    onRemoveAttachment: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            actions.forEach { action ->
                AssistChip(
                    onClick = { onAttachmentActionSelected(action.id) },
                    enabled = !isAttaching,
                    label = { Text(if (isAttaching) "Attaching..." else action.label) }
                )
            }
            if (attachment != null) {
                AssistChip(onClick = onRemoveAttachment, label = { Text("Remove") })
            }
        }

        if (attachment != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = attachment.fileName, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${attachment.typeLabel} | ${attachment.sizeLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentCard(
    attachment: DirectMessageAttachmentUiModel,
    textAlign: TextAlign,
    onOpenAttachment: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.small
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "${attachment.typeLabel} attachment",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = attachment.fileName,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = attachment.sizeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
        if (attachment.downloadUrl.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (textAlign == TextAlign.End) {
                    Arrangement.End
                } else {
                    Arrangement.Start
                }
            ) {
                AssistChip(
                    onClick = { onOpenAttachment(attachment.downloadUrl) },
                    label = { Text("Open") }
                )
            }
        }
    }
}
