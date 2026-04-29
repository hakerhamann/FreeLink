package com.freelink.feature.group.ui

import com.freelink.feature.group.ui.model.GroupItemUiModel

data class GroupUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isCreating: Boolean = false,
    val query: String = "",
    val createDraft: String = "",
    val groups: List<GroupItemUiModel> = emptyList(),
    val errorMessage: String? = null,
    val emptyStateMessage: String? = null
)
