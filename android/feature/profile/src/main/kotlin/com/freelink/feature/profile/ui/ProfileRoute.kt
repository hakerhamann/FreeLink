package com.freelink.feature.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.feature.auth.data.AuthRepository

@Composable
fun ProfileRoute(
    onOpenPrivacySettings: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenMediaGallery: () -> Unit,
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
        onOpenMediaGallery = onOpenMediaGallery,
        onLogout = profileViewModel::logout
    )
}
