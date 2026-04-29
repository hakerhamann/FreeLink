package com.freelink.feature.group.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.group.data.GroupRepository
import com.freelink.feature.group.data.GroupResult
import com.freelink.feature.group.ui.mapper.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupViewModel(
    private val repository: GroupRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    init {
        refresh(isInitial = true)
    }

    fun onQueryChanged(value: String) {
        _uiState.update { it.copy(query = value) }
        refresh(isInitial = false)
    }

    fun onCreateDraftChanged(value: String) {
        _uiState.update { it.copy(createDraft = value) }
    }

    fun refresh() {
        refresh(isInitial = false)
    }

    fun createGroup() {
        val title = _uiState.value.createDraft.trim()
        if (title.length < 2) {
            _uiState.update { it.copy(errorMessage = "Group title should be at least 2 chars.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            when (val result = repository.createGroup(title)) {
                is GroupResult.Success -> {
                    _uiState.update { it.copy(isCreating = false, createDraft = "") }
                    refresh(isInitial = false)
                    selectGroup(result.value.id)
                }
                is GroupResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun selectGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingDetails = true,
                    errorMessage = null
                )
            }

            when (val result = repository.loadGroupDetails(groupId)) {
                is GroupResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDetails = false,
                            selectedGroup = result.value.toUiModel()
                        )
                    }
                }
                is GroupResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDetails = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun refresh(isInitial: Boolean) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = if (isInitial) true else state.isLoading,
                    isRefreshing = !isInitial,
                    errorMessage = null
                )
            }

            when (val result = repository.loadGroups(_uiState.value.query)) {
                is GroupResult.Success -> {
                    val mapped = result.value.map { it.toUiModel() }
                    val preservedSelection = _uiState.value.selectedGroup
                        ?.takeIf { selected -> mapped.any { it.id == selected.groupId } }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            groups = mapped,
                            selectedGroup = preservedSelection,
                            emptyStateMessage = if (mapped.isEmpty()) "No groups found." else null
                        )
                    }
                    if (preservedSelection == null && mapped.isNotEmpty()) {
                        selectGroup(mapped.first().id)
                    }
                }
                is GroupResult.Failure -> {
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

    companion object {
        fun factory(repository: GroupRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    GroupViewModel(repository)
                }
            }
        }
    }
}
