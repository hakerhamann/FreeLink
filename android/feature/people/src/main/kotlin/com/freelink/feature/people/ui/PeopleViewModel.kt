package com.freelink.feature.people.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.people.data.PeopleRepository
import com.freelink.feature.people.data.PeopleResult
import com.freelink.feature.people.ui.mapper.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PeopleViewModel(
    private val repository: PeopleRepository
) : ViewModel() {
    private val queryFlow = MutableStateFlow("")

    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    init {
        observePeople()
        refresh()
    }

    fun onQueryChanged(value: String) {
        queryFlow.value = value
        _uiState.update { it.copy(query = value) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            when (val result = repository.syncPeople()) {
                is PeopleResult.Success -> {
                    _uiState.update { it.copy(isRefreshing = false, errorMessage = null) }
                }

                is PeopleResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun observePeople() {
        viewModelScope.launch {
            combine(repository.peopleFlow, queryFlow) { people, query ->
                if (query.isBlank()) {
                    people
                } else {
                    val normalized = query.trim().lowercase()
                    people.filter {
                        it.displayName.lowercase().contains(normalized) ||
                            it.login.lowercase().contains(normalized)
                    }
                }
            }.collect { filtered ->
                val mapped = filtered.map { it.toUiModel() }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        people = mapped,
                        emptyStateMessage = if (mapped.isEmpty()) "No people found" else null
                    )
                }
            }
        }
    }

    companion object {
        fun factory(repository: PeopleRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    PeopleViewModel(repository)
                }
            }
        }
    }
}
