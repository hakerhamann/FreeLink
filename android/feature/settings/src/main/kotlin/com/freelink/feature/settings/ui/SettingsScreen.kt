package com.freelink.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelink.feature.settings.ui.components.SettingsActionRow
import com.freelink.feature.settings.ui.components.SettingsSectionCard
import com.freelink.feature.settings.ui.components.SettingsSwitchRow
import com.freelink.feature.settings.ui.components.WhoCanMessageChips
import com.freelink.feature.settings.ui.model.WhoCanMessageUiOption

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onRefresh: () -> Unit,
    onHiddenModeChanged: (Boolean) -> Unit,
    onBiometricLockChanged: (Boolean) -> Unit,
    onDisappearingMessagesChanged: (Boolean) -> Unit,
    onLinkPreviewChanged: (Boolean) -> Unit,
    onWhoCanMessageChanged: (WhoCanMessageUiOption) -> Unit,
    onOpenDevices: () -> Unit,
    onLogout: () -> Unit
) {
    val controlsEnabled = !state.isLoading && !state.isSaving && !state.isLoggingOut

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Privacy controls and secure defaults",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                )
            }

            PrivacyLevelChip(label = state.privacyLevelLabel())
        }

        SettingsSectionCard(
            title = state.profileTitle(),
            subtitle = state.profileSubtitle()
        ) {
            Text(
                text = if (state.isSessionAvailable) {
                    "Secure session is active on this device."
                } else {
                    "Sign in to sync protected devices and privacy preferences."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            SettingsActionRow(
                title = "Active devices",
                subtitle = "Manage current sessions and revoke old ones",
                trailing = "Open",
                enabled = controlsEnabled,
                onClick = onOpenDevices
            )
        }

        SettingsSectionCard(
            title = "Privacy and encryption",
            subtitle = "Core protection settings from the MVP security baseline"
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            }

            SettingsSwitchRow(
                title = "Hidden mode",
                subtitle = "Hide content in recent apps and system previews",
                checked = state.settings.hiddenModeEnabled,
                enabled = controlsEnabled,
                onCheckedChange = onHiddenModeChanged
            )
            SettingsSwitchRow(
                title = "Biometric lock",
                subtitle = "Require biometrics to reopen the app",
                checked = state.settings.biometricLockRequired,
                enabled = controlsEnabled,
                onCheckedChange = onBiometricLockChanged
            )
            SettingsSwitchRow(
                title = "Disappearing messages",
                subtitle = "Expire new messages after 24 hours",
                checked = state.settings.disappearingMessagesEnabled,
                enabled = controlsEnabled,
                onCheckedChange = onDisappearingMessagesChanged
            )
            SettingsSwitchRow(
                title = "Link previews",
                subtitle = "Allow metadata previews for shared links",
                checked = state.settings.linkPreviewEnabled,
                enabled = controlsEnabled,
                onCheckedChange = onLinkPreviewChanged
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Who can message me",
                    style = MaterialTheme.typography.titleSmall
                )
                WhoCanMessageChips(
                    selected = state.settings.whoCanMessageMe,
                    enabled = controlsEnabled,
                    onSelected = onWhoCanMessageChanged
                )
            }

            if (state.isSaving) {
                Text(
                    text = "Saving settings...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        SettingsSectionCard(
            title = "App and data",
            subtitle = "Navigation targets for nearby settings that follow in later sprints"
        ) {
            SettingsActionRow(
                title = "Notifications",
                subtitle = "Sounds, vibration and badge preferences",
                trailing = "Soon",
                enabled = false
            )
            SettingsActionRow(
                title = "Appearance",
                subtitle = "Theme, atmosphere and chat visuals",
                trailing = "Soon",
                enabled = false
            )
            SettingsActionRow(
                title = "Archive",
                subtitle = "Archived chats and protected history access",
                trailing = "Sprint 6",
                enabled = false
            )
            SettingsActionRow(
                title = "Media library",
                subtitle = "Shared photos, files and voice attachments",
                trailing = "Sprint 5",
                enabled = false
            )
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedButton(
            onClick = onRefresh,
            enabled = controlsEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh settings")
        }

        Button(
            onClick = onLogout,
            enabled = controlsEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoggingOut) "Logging out..." else "Logout")
        }
    }
}

@Composable
private fun PrivacyLevelChip(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Privacy: $label",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
