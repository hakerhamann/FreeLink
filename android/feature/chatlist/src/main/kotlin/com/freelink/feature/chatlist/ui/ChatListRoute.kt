package com.freelink.feature.chatlist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freelink.core.database.chatlist.db.FreeLinkDatabaseFactory
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.chatlist.KtorChatListApiClient
import com.freelink.core.network.chatlist.ws.KtorChatListWsEventsClient
import com.freelink.core.network.people.KtorPeopleApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.chatlist.data.ChatListRepository

@Composable
fun ChatListRoute(
    onOpenChat: (chatId: String, chatTitle: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current.applicationContext
    val authRepository = remember {
        AuthRepository(apiClient = KtorAuthApiClient(), sessionStore = AuthSessionStore.create(context))
    }
    val database = remember { FreeLinkDatabaseFactory.create(context) }
    val repository = remember {
        ChatListRepository(
            authRepository = authRepository,
            chatListApiClient = KtorChatListApiClient(),
            chatListWsEventsClient = KtorChatListWsEventsClient(),
            chatSummaryDao = database.chatSummaryDao(),
            peopleApiClient = KtorPeopleApiClient(),
            personSummaryDao = database.personSummaryDao()
        )
    }

    val viewModel: ChatListViewModel = viewModel(factory = ChatListViewModel.factory(repository))
    val state by viewModel.uiState.collectAsState()

    ChatListScreen(
        state = state,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onUnreadOnlyChanged = viewModel::onUnreadOnlyChanged,
        onRefresh = viewModel::refresh,
        onArchiveChat = viewModel::archiveChat,
        onOpenChat = onOpenChat
    )
}

@Composable
private fun ChatListScreen(
    state: ChatListUiState,
    onSearchQueryChanged: (String) -> Unit,
    onUnreadOnlyChanged: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onArchiveChat: (chatId: String) -> Unit,
    onOpenChat: (chatId: String, chatTitle: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ChatListHeader() }
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text("Поиск по чатам и людям") }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = state.unreadOnly, onCheckedChange = onUnreadOnlyChanged)
                        Text(text = "Непрочитанные", style = MaterialTheme.typography.bodyMedium)
                    }
                    AssistChip(onClick = onRefresh, label = { Text(if (state.isRefreshing) "Обновляем" else "Обновить") })
                }
            }
            if (state.isLoading) {
                item { CircularProgressIndicator() }
            }
            if (state.errorMessage != null) {
                item { Text(state.errorMessage, color = MaterialTheme.colorScheme.error) }
            }
            if (state.emptyStateMessage != null) {
                item { Text(state.emptyStateMessage, style = MaterialTheme.typography.bodyMedium) }
            }
            if (state.pinnedChats.isNotEmpty()) {
                item { SectionTitle(title = "Закреплённые") }
                items(state.pinnedChats, key = { it.id }) { item ->
                    ChatListItem(item, onClick = { onOpenChat(item.id, item.title) }, onArchive = { onArchiveChat(item.id) })
                }
            }
            if (state.otherChats.isNotEmpty()) {
                item { SectionTitle(title = "Все чаты") }
                items(state.otherChats, key = { it.id }) { item ->
                    ChatListItem(item, onClick = { onOpenChat(item.id, item.title) }, onArchive = { onArchiveChat(item.id) })
                }
            }
            if (state.peopleMatches.isNotEmpty()) {
                item { SectionTitle(title = "Люди") }
                items(state.peopleMatches, key = { it.id }) { person -> PersonSearchItem(person) }
            }
        }
    }
}

@Composable
private fun ChatListHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "SVOi",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Защищённые чаты и близкие люди",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
