package com.freelink.core.network.people

import com.freelink.core.model.domain.User
import com.freelink.core.network.auth.AuthApiResult

interface PeopleApiClient {
    suspend fun fetchPeople(accessToken: String): AuthApiResult<List<User>>
}
