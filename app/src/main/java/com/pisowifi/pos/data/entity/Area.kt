package com.pisowifi.pos.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "areas")
data class Area(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "share_given", defaultValue = "0") val shareGiven: Boolean = false,
    @ColumnInfo(name = "last_shared_date") val lastSharedDate: Long = 0L
)
