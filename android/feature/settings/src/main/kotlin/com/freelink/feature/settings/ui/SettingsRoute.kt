package com.freelink.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.datastore.privacy.PrivacySettingsStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.privacy.KtorPrivacyApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.settings.data.PrivacySettingsRepository
import com.freelink.feature.settings.ui.model.WhoCanMessageUiOption

@Composable
fun SettingsRoute(
    onOpenDevices: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val authRepository = remember {
        AuthRepository(
            apiClient = KtorAuthApiClient(),
            sessionStore = AuthSessionStore.create(context)
        )
    }
    val repository = remember {
        PrivacySettingsRepository(
            authRepository = authRepository,
            privacyApiClient = KtorPrivacyApiClient(),
            settingsStore = PrivacySettingsStore.create(context)
        )
    }

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(repository)
    )

    val state by settingsViewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLoggedOut()
        }
    }

    SettingsScreen(
        state = state,
        onRefresh = settingsViewModel::refresh,
        onHiddenModeChanged = settingsViewModel::onHiddenModeChanged,
        onBiometricLockChanged = settingsViewModel::onBiometricLockChanged,
        onDisappearingMessagesChanged = settingsViewModel::onDisappearingMessagesChanged,
        onLinkPreviewChanged = settingsViewModel::onLinkPreviewChanged,
        onWhoCanMessageChanged = settingsViewModel::onWhoCanMessageChanged,
        onOpenDevices = onOpenDevices,
        onLogout = settingsViewModel::logout
    )
}

@Composable
private fun SettingsScreen(
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Privacy and Security",
            style = MaterialTheme.typography.headlineSmall
        )

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        SettingSwitchRow(
            title = "Hidden mode",
            subtitle = "Hide content in system previews",
            checked = state.settings.hiddenModeEnabled,
            enabled = !state.isLoading && !state.isSaving,
            onCheckedChange = onHiddenModeChanged
        )
        SettingSwitchRow(
            title = "Biometric lock",
            subtitle = "Require biometrics to open the app",
            checked = state.settings.biometricLockRequired,
            enabled = !state.isLoading && !state.isSaving,
            onCheckedChange = onBiometricLockChanged
        )
        SettingSwitchRow(
            title = "Disappearing messages",
            subtitle = "New messages expire after 24 hours",
            checked = state.settings.disappearingMessagesEnabled,
            enabled = !state.isLoading && !state.isSaving,
            onCheckedChange = onDisappearingMessagesChanged
        )
        SettingSwitchRow(
            title = "Link previews",
            subtitle = "Show preview metadata for links",
            checked = state.settings.linkPreviewEnabled,
            enabled = !state.isLoading && !state.isSaving,
            onCheckedChange = onLinkPreviewChanged
        )

        HorizontalDivider()

        Text(
            text = "Who can message me",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WhoCanMessageUiOption.entries.forEach { option ->
                val isSelected = option == state.settings.whoCanMessageMe

                if (isSelected) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onWhoCanMessageChanged(option) },
                        enabled = !state.isLoading && !state.isSaving
                    ) {
                        Text(option.title)
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onWhoCanMessageChanged(option) },
                        enabled = !state.isLoading && !state.isSaving
                    ) {
                        Text(option.title)
                    }
                }
            }
        }

        if (state.isSaving) {
            Text(
                text = "Saving settings...",
                style = MaterialTheme.typography.bodySmall
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
            enabled = !state.isLoading && !state.isSaving && !state.isLoggingOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh settings")
        }

        OutlinedButton(
            onClick = onOpenDevices,
            enabled = !state.isLoading && !state.isSaving && !state.isLoggingOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Active devices")
        }

        Button(
            onClick = onLogout,
            enabled = !state.isLoading && !state.isSaving && !state.isLoggingOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoggingOut) "Logging out..." else "Logout")
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}
