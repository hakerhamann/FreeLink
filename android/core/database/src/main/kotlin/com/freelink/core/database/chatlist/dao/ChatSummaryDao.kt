package com.freelink.core.database.chatlist.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.freelink.core.database.chatlist.entity.ChatSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSummaryDao {
    @Query("SELECT * FROM chat_summaries ORDER BY isPinned DESC, updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<ChatSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ChatSummaryEntity>)

    @Query("DELETE FROM chat_summaries")
    suspend fun clearAll()

    @Query("DELETE FROM chat_summaries WHERE id = :chatId")
    suspend fun deleteById(chatId: String)
}
