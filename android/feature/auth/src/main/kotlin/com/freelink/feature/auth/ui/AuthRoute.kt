package com.freelink.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.feature.auth.data.AuthRepository

@Composable
fun AuthRoute(
    onAuthorized: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val authRepository = remember {
        AuthRepository(
            apiClient = KtorAuthApiClient(),
            sessionStore = AuthSessionStore.create(context)
        )
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(authRepository)
    )

    val state by authViewModel.uiState.collectAsState()

    LaunchedEffect(state.isAuthorized) {
        if (state.isAuthorized) {
            onAuthorized()
        }
    }

    AuthScreen(
        state = state,
        onLoginChanged = authViewModel::onLoginChanged,
        onPasswordChanged = authViewModel::onPasswordChanged,
        onLoginClick = authViewModel::submitLogin
    )
}

@Composable
private fun AuthScreen(
    state: AuthUiState,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FreeLink",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Вход по логину и паролю",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = state.login,
            onValueChange = onLoginChanged,
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        if (state.knownDevicesCount > 0) {
            Text(
                text = "Активных устройств: ${state.knownDevicesCount}",
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

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text(text = if (state.isLoading) "Вход..." else "Войти")
        }
    }
}
