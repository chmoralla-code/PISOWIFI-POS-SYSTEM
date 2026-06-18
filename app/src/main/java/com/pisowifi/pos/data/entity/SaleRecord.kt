package com.pisowifi.pos.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_records",
    foreignKeys = [ForeignKey(
        entity = PisowifiDevice::class,
        parentColumns = ["id"],
        childColumns = ["pisowifi_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("pisowifi_id"), Index("date")]
)
data class SaleRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pisowifi_id") val pisowifiId: Int = 0,
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)
