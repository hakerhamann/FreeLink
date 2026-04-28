package com.freelink.feature.people.ui

import com.freelink.feature.people.ui.model.PersonItemUiModel

data class PeopleUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val query: String = "",
    val people: List<PersonItemUiModel> = emptyList(),
    val errorMessage: String? = null,
    val emptyStateMessage: String? = null
)
