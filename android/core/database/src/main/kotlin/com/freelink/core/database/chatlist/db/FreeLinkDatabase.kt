package com.freelink.core.database.chatlist.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freelink.core.database.chatlist.dao.ChatSummaryDao
import com.freelink.core.database.chatlist.entity.ChatSummaryEntity
import com.freelink.core.database.people.dao.PersonSummaryDao
import com.freelink.core.database.people.entity.PersonSummaryEntity

@Database(
    entities = [ChatSummaryEntity::class, PersonSummaryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FreeLinkDatabase : RoomDatabase() {
    abstract fun chatSummaryDao(): ChatSummaryDao
    abstract fun personSummaryDao(): PersonSummaryDao
}
