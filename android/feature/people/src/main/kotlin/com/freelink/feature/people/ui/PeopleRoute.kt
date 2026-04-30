package com.freelink.feature.people.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.freelink.core.network.people.KtorPeopleApiClient
import com.freelink.feature.auth.data.AuthRepository
import com.freelink.feature.people.data.PeopleRepository
import com.freelink.feature.people.ui.model.PersonItemUiModel

@Composable
fun PeopleRoute() {
    val context = LocalContext.current.applicationContext
    val authRepository = remember { AuthRepository(KtorAuthApiClient(), AuthSessionStore.create(context)) }
    val database = remember { FreeLinkDatabaseFactory.create(context) }
    val repository = remember {
        PeopleRepository(authRepository, KtorPeopleApiClient(), database.personSummaryDao())
    }

    val viewModel: PeopleViewModel = viewModel(factory = PeopleViewModel.factory(repository))
    val state by viewModel.uiState.collectAsState()
    PeopleScreen(state = state, onQueryChanged = viewModel::onQueryChanged, onRefresh = viewModel::refresh)
}

@Composable
private fun PeopleScreen(
    state: PeopleUiState,
    onQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Люди", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("Близкие и доверенные контакты", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                    AssistChip(onClick = onRefresh, label = { Text(if (state.isRefreshing) "Обновляем" else "Обновить") })
                }
            }
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text("Поиск людей") }
                )
            }
            if (state.isLoading) item { CircularProgressIndicator() }
            if (state.errorMessage != null) item { Text(state.errorMessage, color = MaterialTheme.colorScheme.error) }
            if (state.emptyStateMessage != null) item { Text(state.emptyStateMessage) }
            items(state.people, key = { it.id }) { person -> PersonItem(person) }
        }
    }
}

@Composable
private fun PersonItem(person: PersonItemUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(person.displayName.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(person.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(person.login, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
            }
            Text(
                text = person.status,
                style = MaterialTheme.typography.labelMedium,
                color = if (person.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}
