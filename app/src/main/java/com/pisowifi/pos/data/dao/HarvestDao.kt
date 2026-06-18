package com.pisowifi.pos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pisowifi.pos.data.entity.HarvestRecord
import kotlinx.coroutines.flow.Flow

data class PisowifiHarvestSummary(
    val id: Int,
    val pisowifiId: Int,
    val deviceName: String,
    val areaName: String?,
    val harvestDate: Long,
    val totalSales: Double,
    val shareGiven: Boolean,
    val shareAmount: Double
)

@Dao
interface HarvestDao {
    @Query("SELECT * FROM harvest_records ORDER BY harvest_date DESC")
    fun getAll(): Flow<List<HarvestRecord>>

    @Query("""
        SELECT h.id, h.pisowifi_id AS pisowifiId, p.name AS deviceName,
               a.name AS areaName, h.harvest_date AS harvestDate,
               h.total_sales AS totalSales, h.share_given AS shareGiven,
               h.share_amount AS shareAmount
        FROM harvest_records h
        INNER JOIN pisowifi_devices p ON h.pisowifi_id = p.id
        LEFT JOIN areas a ON p.area_id = a.id
        ORDER BY h.harvest_date DESC
    """)
    fun getAllWithDetails(): Flow<List<PisowifiHarvestSummary>>

    @Query("SELECT * FROM harvest_records WHERE share_given = 0 ORDER BY harvest_date ASC")
    fun getPendingShares(): Flow<List<HarvestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HarvestRecord): Long

    @Update
    suspend fun update(record: HarvestRecord)

    @Query("UPDATE harvest_records SET share_given = :given WHERE id = :id")
    suspend fun updateShareGiven(id: Int, given: Boolean)

    @Query("DELETE FROM harvest_records WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM sale_records WHERE pisowifi_id = :pisowifiId AND date >= :since")
    suspend fun getSalesSince(pisowifiId: Int, since: Long): Double
}
