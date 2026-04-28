package com.freelink.feature.devices.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.freelink.core.model.domain.auth.DeviceSession
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.feature.devices.data.DeviceSessionsRepository

@Composable
fun DeviceSessionsRoute(
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val repository = remember {
        DeviceSessionsRepository(
            authApiClient = KtorAuthApiClient(),
            authSessionStore = AuthSessionStore.create(context)
        )
    }

    val deviceSessionsViewModel: DeviceSessionsViewModel = viewModel(
        factory = DeviceSessionsViewModel.factory(repository)
    )

    val state by deviceSessionsViewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLoggedOut()
        }
    }

    DeviceSessionsScreen(
        state = state,
        onRefresh = deviceSessionsViewModel::refresh,
        onRevokeDevice = deviceSessionsViewModel::revokeDevice,
        onLogout = deviceSessionsViewModel::logoutCurrentSession
    )
}

@Composable
private fun DeviceSessionsScreen(
    state: DeviceSessionsUiState,
    onRefresh: () -> Unit,
    onRevokeDevice: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Активные устройства",
            style = MaterialTheme.typography.headlineSmall
        )

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        if (!state.isLoading && state.sessions.isEmpty()) {
            Text(
                text = "Нет активных устройств",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        state.sessions.forEach { session ->
            DeviceSessionCard(
                session = session,
                revoking = state.revokingDeviceId == session.deviceId,
                onRevoke = onRevokeDevice
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
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && !state.isLoggingOut
        ) {
            Text("Обновить")
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && !state.isLoggingOut
        ) {
            Text(if (state.isLoggingOut) "Выходим..." else "Выйти")
        }
    }
}

@Composable
private fun DeviceSessionCard(
    session: DeviceSession,
    revoking: Boolean,
    onRevoke: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = session.deviceName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Последняя активность: ${session.lastSeenAtIso}",
                style = MaterialTheme.typography.bodySmall
            )

            if (session.isCurrent) {
                Text(
                    text = "Текущее устройство",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = { onRevoke(session.deviceId) },
                    enabled = !revoking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (revoking) "Отзываем..." else "Завершить сессию")
                }
            }
        }
    }
}
