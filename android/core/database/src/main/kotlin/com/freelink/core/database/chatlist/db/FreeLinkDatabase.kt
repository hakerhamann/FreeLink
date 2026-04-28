package com.freelink.core.database.chatlist.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freelink.core.database.chatlist.dao.ChatSummaryDao
import com.freelink.core.database.chatlist.entity.ChatSummaryEntity

@Database(
    entities = [ChatSummaryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FreeLinkDatabase : RoomDatabase() {
    abstract fun chatSummaryDao(): ChatSummaryDao
}
