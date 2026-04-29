package com.freelink.feature.group.ui

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
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.groups.KtorGroupApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.group.data.GroupRepository
import com.freelink.feature.group.ui.model.GroupItemUiModel

@Composable
fun GroupRoute() {
    val context = LocalContext.current.applicationContext
    val authRepository = remember {
        AuthRepository(
            apiClient = KtorAuthApiClient(),
            sessionStore = AuthSessionStore.create(context)
        )
    }
    val repository = remember {
        GroupRepository(
            authRepository = authRepository,
            groupApiClient = KtorGroupApiClient()
        )
    }

    val viewModel: GroupViewModel = viewModel(
        factory = GroupViewModel.factory(repository)
    )
    val state by viewModel.uiState.collectAsState()

    GroupScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onCreateDraftChanged = viewModel::onCreateDraftChanged,
        onCreate = viewModel::createGroup,
        onRefresh = viewModel::refresh
    )
}

@Composable
private fun GroupScreen(
    state: GroupUiState,
    onQueryChanged: (String) -> Unit,
    onCreateDraftChanged: (String) -> Unit,
    onCreate: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spaces",
                style = MaterialTheme.typography.headlineSmall
            )
            AssistChip(
                onClick = onRefresh,
                label = { Text(if (state.isRefreshing) "Refreshing..." else "Refresh") }
            )
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search groups") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.createDraft,
                onValueChange = onCreateDraftChanged,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("New group title") }
            )
            AssistChip(
                onClick = onCreate,
                enabled = !state.isCreating,
                label = { Text(if (state.isCreating) "Creating..." else "Create") }
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
            items(state.groups, key = { it.id }) { group ->
                GroupItem(group)
            }
        }
    }
}

@Composable
private fun GroupItem(item: GroupItemUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.roleLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = item.membersLabel,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = item.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        HorizontalDivider()
    }
}
