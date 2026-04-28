package com.freelink.core.database.people.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.freelink.core.database.people.entity.PersonSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonSummaryDao {
    @Query("SELECT * FROM people_summaries ORDER BY isOnline DESC, displayName ASC")
    fun observeAll(): Flow<List<PersonSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PersonSummaryEntity>)

    @Query("DELETE FROM people_summaries")
    suspend fun clearAll()
}
