package com.freelink.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freelink.feature.chat.ui.model.DirectMessageAttachmentKindUiModel
import com.freelink.feature.chat.ui.model.DirectMessageAttachmentUiModel

@Composable
fun AttachmentCard(
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
        AttachmentPreview(attachment = attachment, textAlign = textAlign)
        if (attachment.downloadUrl.isNotBlank()) {
            AttachmentOpenAction(
                attachment = attachment,
                textAlign = textAlign,
                onOpenAttachment = onOpenAttachment
            )
        }
    }
}

@Composable
private fun AttachmentPreview(
    attachment: DirectMessageAttachmentUiModel,
    textAlign: TextAlign
) {
    when (attachment.kind) {
        DirectMessageAttachmentKindUiModel.PHOTO,
        DirectMessageAttachmentKindUiModel.VIDEO -> MediaPreviewStub(
            attachment = attachment,
            textAlign = textAlign
        )
        DirectMessageAttachmentKindUiModel.VOICE -> VoiceWaveformStub(
            bars = attachment.waveformBars,
            durationLabel = attachment.durationLabel.orEmpty()
        )
        DirectMessageAttachmentKindUiModel.FILE -> FilePreviewStub(
            attachment = attachment,
            textAlign = textAlign
        )
    }
}

@Composable
private fun MediaPreviewStub(
    attachment: DirectMessageAttachmentUiModel,
    textAlign: TextAlign
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.small
            )
            .padding(10.dp),
        contentAlignment = if (textAlign == TextAlign.End) Alignment.BottomEnd else Alignment.BottomStart
    ) {
        Text(
            text = "${attachment.previewLabel} | ${attachment.sizeLabel}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun FilePreviewStub(
    attachment: DirectMessageAttachmentUiModel,
    textAlign: TextAlign
) {
    Text(
        text = "${attachment.previewLabel} | ${attachment.sizeLabel}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AttachmentOpenAction(
    attachment: DirectMessageAttachmentUiModel,
    textAlign: TextAlign,
    onOpenAttachment: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (textAlign == TextAlign.End) Arrangement.End else Arrangement.Start
    ) {
        AssistChip(
            onClick = { onOpenAttachment(attachment.downloadUrl) },
            label = {
                Text(
                    if (attachment.kind == DirectMessageAttachmentKindUiModel.VOICE) {
                        "Open audio"
                    } else {
                        "Open"
                    }
                )
            }
        )
    }
}

@Composable
private fun VoiceWaveformStub(
    bars: List<Int>,
    durationLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bars.forEach { heightValue ->
                WaveformBar(heightValue = heightValue)
            }
        }
        Text(
            text = durationLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun RowScope.WaveformBar(heightValue: Int) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(heightValue.dp)
            .width(4.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                shape = MaterialTheme.shapes.small
            )
    )
}
