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
    onOpenMediaGallery: () -> Unit,
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
            Text(state.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(state.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f))
        }

        ProfileSectionCard(title = "Защищённая сессия", trailing = if (state.isSessionAvailable) "Активна" else "Не в сети") {
            Text(
                text = if (state.isSessionAvailable) {
                    "Текущее устройство ${state.deviceId.orEmpty().takeLast(6)} связано с приватным аккаунтом."
                } else {
                    "Детали сессии появятся после входа."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onOpenPrivacySettings, modifier = Modifier.weight(1f), enabled = !state.isLoggingOut) { Text("Приватность") }
                OutlinedButton(onClick = onOpenDevices, modifier = Modifier.weight(1f), enabled = !state.isLoggingOut) { Text("Устройства") }
            }
        }

        ProfileSectionCard(title = "Общие чаты", trailing = "Все") { state.sharedChats.forEach { SharedChatRow(it) } }
        ProfileSectionCard(title = "Общие чаты", trailing = state.sharedPhotos.size.toString()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.sharedPhotos, key = { it.id }) { SharedPhotoTile(it) }
            }
            OutlinedButton(onClick = onOpenMediaGallery, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoggingOut) { Text("Открыть медиагалерею") }
        }
        ProfileSectionCard(title = "Близкие люди", trailing = state.trustedContacts.size.toString()) {
            state.trustedContacts.forEach { TrustedContactRow(it) }
        }
        ProfileSectionCard(title = "Прикреплённые сообщения", trailing = state.pinnedMessages.size.toString()) {
            state.pinnedMessages.forEach { PinnedMessageRow(it) }
        }
        if (state.errorMessage != null) Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoggingOut) {
            Text(if (state.isLoggingOut) "Выходим..." else "Выйти")
        }
    }
}
