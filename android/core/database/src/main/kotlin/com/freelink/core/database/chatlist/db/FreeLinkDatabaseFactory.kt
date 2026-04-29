package com.freelink.core.database.chatlist.db

import android.content.Context
import androidx.room.Room

object FreeLinkDatabaseFactory {
    fun create(context: Context): FreeLinkDatabase {
        return Room.databaseBuilder(
            context,
            FreeLinkDatabase::class.java,
            "freelink.db"
        ).fallbackToDestructiveMigration().build()
    }
}
