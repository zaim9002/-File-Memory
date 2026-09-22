package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CleanRecordDao {

    @Query("SELECT * FROM clean_records ORDER BY timestamp DESC LIMIT 50")
    fun getAllRecords(): Flow<List<CleanRecord>>

    @Query("SELECT * FROM clean_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecord(): Flow<CleanRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CleanRecord): Long

    @Query("SELECT SUM(freedBytes) FROM clean_records")
    fun getTotalFreedBytes(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM clean_records")
    fun getTotalCleanCount(): Flow<Int>

    @Query("DELETE FROM clean_records")
    suspend fun clearAllRecords()
}
