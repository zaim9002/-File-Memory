package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clean_records")
data class CleanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val ramBeforeBytes: Long,
    val ramAfterBytes: Long,
    val freedBytes: Long,
    val cleanType: String,
    val processesKilled: Int,
    val message: String
)
