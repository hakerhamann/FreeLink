package com.freelink.feature.media_gallery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.model.domain.AttachmentType
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.media.KtorMediaApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.media_gallery.data.MediaGalleryRepository

@Composable
fun MediaGalleryRoute(
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
        MediaGalleryRepository(
            authRepository = authRepository,
            mediaApiClient = KtorMediaApiClient()
        )
    }
    val viewModel: MediaGalleryViewModel = viewModel(
        factory = MediaGalleryViewModel.factory(repository)
    )
    val state by viewModel.uiState.collectAsState()

    MediaGalleryScreen(
        state = state,
        onBack = onBack,
        onFileNameChanged = viewModel::onFileNameChanged,
        onMimeTypeChanged = viewModel::onMimeTypeChanged,
        onDigestChanged = viewModel::onDigestChanged,
        onByteSizeChanged = viewModel::onByteSizeChanged,
        onAttachmentTypeChanged = viewModel::onAttachmentTypeChanged,
        onInitUpload = viewModel::initUpload,
        onCompleteUpload = viewModel::completeUpload
    )
}

@Composable
private fun MediaGalleryScreen(
    state: MediaGalleryUiState,
    onBack: () -> Unit,
    onFileNameChanged: (String) -> Unit,
    onMimeTypeChanged: (String) -> Unit,
    onDigestChanged: (String) -> Unit,
    onByteSizeChanged: (String) -> Unit,
    onAttachmentTypeChanged: (AttachmentType) -> Unit,
    onInitUpload: () -> Unit,
    onCompleteUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Media gallery",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            AssistChip(
                onClick = onBack,
                label = { Text("Back") }
            )
        }

        Text(
            text = "Sprint 5 start: init and complete upload sessions for deduplicated media.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )

        OutlinedTextField(
            value = state.fileName,
            onValueChange = onFileNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("File name") },
            singleLine = true
        )
        OutlinedTextField(
            value = state.mimeType,
            onValueChange = onMimeTypeChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("MIME type") },
            singleLine = true
        )
        OutlinedTextField(
            value = state.digestSha256,
            onValueChange = onDigestChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Digest SHA-256") },
            singleLine = true
        )
        OutlinedTextField(
            value = state.byteSizeText,
            onValueChange = onByteSizeChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Byte size") },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AttachmentType.entries.forEach { type ->
                AssistChip(
                    onClick = { onAttachmentTypeChanged(type) },
                    label = { Text(type.name) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = onInitUpload,
                enabled = !state.isSubmitting,
                label = { Text("Init upload") }
            )
            AssistChip(
                onClick = onCompleteUpload,
                enabled = !state.isSubmitting && state.latestSession != null,
                label = { Text("Complete upload") }
            )
        }

        state.latestSession?.let { session ->
            Text(
                text = "Session ${session.uploadId} | exists=${session.alreadyExists} | key=${session.blobKey}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        state.latestMedia?.let { media ->
            Text(
                text = "Stored ${media.fileName} as ${media.attachmentId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = media.downloadUrl,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
