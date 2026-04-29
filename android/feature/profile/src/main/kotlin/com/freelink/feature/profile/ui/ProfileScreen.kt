package com.freelink.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelink.feature.profile.ui.components.PinnedMessageRow
import com.freelink.feature.profile.ui.components.ProfileSectionCard
import com.freelink.feature.profile.ui.components.SharedChatRow
import com.freelink.feature.profile.ui.components.SharedPhotoTile
import com.freelink.feature.profile.ui.components.TrustedContactRow

@Composable
internal fun ProfileScreen(
    state: ProfileUiState,
    onOpenPrivacySettings: () -> Unit,
    onOpenDevices: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }

        ProfileSectionCard(
            title = "Secure session",
            trailing = if (state.isSessionAvailable) "Protected" else "Offline"
        ) {
            Text(
                text = if (state.isSessionAvailable) {
                    "Current device ${state.deviceId.orEmpty().takeLast(6)} is linked to your private account context."
                } else {
                    "Session details will appear here after sign in."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenPrivacySettings,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoggingOut
                ) {
                    Text("Privacy")
                }
                OutlinedButton(
                    onClick = onOpenDevices,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoggingOut
                ) {
                    Text("Devices")
                }
            }
        }

        ProfileSectionCard(
            title = "Shared chats",
            trailing = "All"
        ) {
            state.sharedChats.forEach { item ->
                SharedChatRow(item)
            }
        }

        ProfileSectionCard(
            title = "Shared photos",
            trailing = state.sharedPhotos.size.toString()
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.sharedPhotos, key = { it.id }) { item ->
                    SharedPhotoTile(
                        item = item,
                        modifier = Modifier
                    )
                }
            }
        }

        ProfileSectionCard(
            title = "Trusted contacts",
            trailing = state.trustedContacts.size.toString()
        ) {
            state.trustedContacts.forEach { item ->
                TrustedContactRow(item)
            }
        }

        ProfileSectionCard(
            title = "Pinned messages",
            trailing = state.pinnedMessages.size.toString()
        ) {
            state.pinnedMessages.forEach { item ->
                PinnedMessageRow(item)
            }
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoggingOut
        ) {
            Text(if (state.isLoggingOut) "Logging out..." else "Logout")
        }
    }
}
