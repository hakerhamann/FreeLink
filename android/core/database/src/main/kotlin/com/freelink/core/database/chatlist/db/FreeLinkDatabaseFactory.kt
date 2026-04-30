package com.freelink.core.database.chatlist.db

import android.content.Context
import androidx.room.Room

object FreeLinkDatabaseFactory {
    @Volatile
    private var instance: FreeLinkDatabase? = null

    fun create(context: Context): FreeLinkDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context.applicationContext).also { database ->
                instance = database
            }
        }
    }

    private fun buildDatabase(context: Context): FreeLinkDatabase {
        return Room.databaseBuilder(
            context,
            FreeLinkDatabase::class.java,
            "freelink.db"
        ).fallbackToDestructiveMigration().build()
    }
}
