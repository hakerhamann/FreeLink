package com.freelink.feature.people.ui

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
import com.freelink.core.database.chatlist.db.FreeLinkDatabaseFactory
import com.freelink.core.datastore.auth.AuthSessionStore
import com.freelink.core.network.auth.KtorAuthApiClient
import com.freelink.core.network.people.KtorPeopleApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.people.data.PeopleRepository
import com.freelink.feature.people.ui.model.PersonItemUiModel

@Composable
fun PeopleRoute() {
    val context = LocalContext.current.applicationContext
    val authRepository = remember {
        AuthRepository(
            apiClient = KtorAuthApiClient(),
            sessionStore = AuthSessionStore.create(context)
        )
    }
    val database = remember { FreeLinkDatabaseFactory.create(context) }
    val repository = remember {
        PeopleRepository(
            authRepository = authRepository,
            peopleApiClient = KtorPeopleApiClient(),
            personSummaryDao = database.personSummaryDao()
        )
    }

    val viewModel: PeopleViewModel = viewModel(
        factory = PeopleViewModel.factory(repository)
    )
    val state by viewModel.uiState.collectAsState()

    PeopleScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onRefresh = viewModel::refresh
    )
}

@Composable
private fun PeopleScreen(
    state: PeopleUiState,
    onQueryChanged: (String) -> Unit,
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
                text = "People",
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
            label = { Text("Search people") }
        )

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
            items(state.people, key = { it.id }) { person ->
                PersonItem(person = person)
            }
        }
    }
}

@Composable
private fun PersonItem(person: PersonItemUiModel) {
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
                text = person.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = person.status,
                style = MaterialTheme.typography.labelMedium,
                color = if (person.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
        Text(
            text = person.login,
            style = MaterialTheme.typography.bodySmall
        )
        HorizontalDivider()
    }
}
