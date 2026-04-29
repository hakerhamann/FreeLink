package com.freelink.feature.archive.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.chatlist.KtorChatListApiClient
import com.freelink.feature.archive.data.ArchiveRepository
import com.freelink.feature.auth.data.AuthRepository

@Composable
fun ArchiveRoute(
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
        ArchiveRepository(
            authRepository = authRepository,
            chatListApiClient = KtorChatListApiClient()
        )
    }
    val viewModel: ArchiveViewModel = viewModel(
        factory = ArchiveViewModel.factory(repository)
    )
    val state by viewModel.uiState.collectAsState()

    ArchiveScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onRestoreChat = viewModel::restoreChat
    )
}
