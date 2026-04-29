package com.freelink.core.network.people.mapper

import com.freelink.core.model.domain.User
import com.freelink.core.network.people.dto.PersonSummaryDto

fun PersonSummaryDto.toDomain(): User {
    return User(
        id = id,
        login = login,
        displayName = displayName,
        isOnline = isOnline
    )
}
