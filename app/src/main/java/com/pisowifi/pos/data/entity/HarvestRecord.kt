package com.pisowifi.pos.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "harvest_records",
    foreignKeys = [ForeignKey(
        entity = PisowifiDevice::class,
        parentColumns = ["id"],
        childColumns = ["pisowifi_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("pisowifi_id")]
)
data class HarvestRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pisowifi_id") val pisowifiId: Int = 0,
    @ColumnInfo(name = "harvest_date") val harvestDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "total_sales") val totalSales: Double = 0.0,
    @ColumnInfo(name = "share_given") val shareGiven: Boolean = false,
    @ColumnInfo(name = "share_amount") val shareAmount: Double = 0.0
)
