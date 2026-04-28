package com.freelink.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.feature.auth.data.AuthRepository

@Composable
fun ProfileRoute(
    onOpenPrivacySettings: () -> Unit,
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

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(authRepository)
    )
    val state by profileViewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLoggedOut()
        }
    }

    ProfileScreen(
        state = state,
        onOpenPrivacySettings = onOpenPrivacySettings,
        onOpenDevices = onOpenDevices,
        onLogout = profileViewModel::logout
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileUiState,
    onOpenPrivacySettings: () -> Unit,
    onOpenDevices: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall
        )

        if (state.isSessionAvailable) {
            Text(
                text = "User ID: ${state.userId}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Current device ID: ${state.deviceId}",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text(
                text = "Session not found",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedButton(
            onClick = onOpenPrivacySettings,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoggingOut
        ) {
            Text("Privacy settings")
        }

        OutlinedButton(
            onClick = onOpenDevices,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoggingOut
        ) {
            Text("Active devices")
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
