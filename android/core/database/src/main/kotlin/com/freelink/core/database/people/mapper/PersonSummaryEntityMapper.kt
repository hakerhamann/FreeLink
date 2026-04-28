package com.freelink.core.database.people.mapper

import com.freelink.core.database.people.entity.PersonSummaryEntity
import com.freelink.core.model.domain.User

fun PersonSummaryEntity.toDomain(): User {
    return User(
        id = id,
        login = login,
        displayName = displayName,
        isOnline = isOnline
    )
}

fun User.toEntity(): PersonSummaryEntity {
    return PersonSummaryEntity(
        id = id,
        login = login,
        displayName = displayName,
        isOnline = isOnline
    )
}
