package com.pisowifi.pos.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pisowifi_devices")
data class PisowifiDevice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    @ColumnInfo(name = "area_id") val areaId: Int = 0,
    val capital: Double = 0.0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
