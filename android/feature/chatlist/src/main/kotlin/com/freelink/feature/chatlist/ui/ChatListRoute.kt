package com.freelink.feature.chatlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.freelink.feature.chatlist.ui.model.ChatListItemUiModel
import com.freelink.feature.chatlist.ui.model.ChatListPersonUiModel

@Composable
fun ChatListRoute() {
    val context = LocalContext.current.applicationContext
    val authRepository = remember {
        AuthRepository(
            apiClient = KtorAuthApiClient(),
            sessionStore = AuthSessionStore.create(context)
        )
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

    val viewModel: ChatListViewModel = viewModel(
        factory = ChatListViewModel.factory(repository)
    )
    val state by viewModel.uiState.collectAsState()

    ChatListScreen(
        state = state,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onUnreadOnlyChanged = viewModel::onUnreadOnlyChanged,
        onRefresh = viewModel::refresh
    )
}

@Composable
private fun ChatListScreen(
    state: ChatListUiState,
    onSearchQueryChanged: (String) -> Unit,
    onUnreadOnlyChanged: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search chats and people") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.unreadOnly,
                    onCheckedChange = onUnreadOnlyChanged
                )
                Text(
                    text = "Unread only",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AssistChip(
                onClick = onRefresh,
                label = { Text(if (state.isRefreshing) "Refreshing..." else "Refresh") }
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (state.emptyStateMessage != null) {
            Text(
                text = state.emptyStateMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (state.pinnedChats.isNotEmpty()) {
                item {
                    SectionTitle(title = "Pinned")
                }
                items(state.pinnedChats, key = { it.id }) { item ->
                    ChatListItem(item = item)
                }
            }

            if (state.otherChats.isNotEmpty()) {
                item {
                    SectionTitle(title = "All chats")
                }
                items(state.otherChats, key = { it.id }) { item ->
                    ChatListItem(item = item)
                }
            }

            if (state.peopleMatches.isNotEmpty()) {
                item {
                    SectionTitle(title = "People")
                }
                items(state.peopleMatches, key = { it.id }) { person ->
                    PersonSearchItem(item = person)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ChatListItem(item: ChatListItemUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.chatTypeLabel,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.unreadBadge != null) {
                    Text(
                        text = item.unreadBadge,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = item.updatedAtLabel,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = item.lastMessagePreview,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )

        HorizontalDivider()
    }
}

@Composable
private fun PersonSearchItem(item: ChatListPersonUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.status,
                style = MaterialTheme.typography.labelSmall,
                color = if (item.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }

        Text(
            text = item.login,
            style = MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider()
    }
}
