package com.freelink.core.database.people.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people_summaries")
data class PersonSummaryEntity(
    @PrimaryKey val id: String,
    val login: String,
    val displayName: String,
    val isOnline: Boolean
)
