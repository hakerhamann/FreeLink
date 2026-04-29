package com.freelink.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.datastore.privacy.PrivacySettingsStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.privacy.KtorPrivacyApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.settings.data.PrivacySettingsRepository

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
        factory = SettingsViewModel.factory(
            repository = repository,
            authRepository = authRepository
        )
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
