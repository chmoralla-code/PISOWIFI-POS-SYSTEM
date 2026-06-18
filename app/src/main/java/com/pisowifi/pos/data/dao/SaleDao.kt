package com.pisowifi.pos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pisowifi.pos.data.entity.SaleRecord
import kotlinx.coroutines.flow.Flow

data class PisowifiSalesSummary(
    val pisowifiId: Int,
    val deviceName: String,
    val total: Double
)

@Dao
interface SaleDao {
    @Query("SELECT * FROM sale_records WHERE pisowifi_id = :pisowifiId ORDER BY date DESC")
    fun getByPisowifi(pisowifiId: Int): Flow<List<SaleRecord>>

    @Query("SELECT * FROM sale_records ORDER BY date DESC LIMIT 100")
    fun getRecent(): Flow<List<SaleRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleRecord): Long

    @Delete
    suspend fun delete(sale: SaleRecord)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM sale_records WHERE date >= :start AND date <= :end")
    fun getTotalSales(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM sale_records WHERE pisowifi_id = :pisowifiId AND date >= :start AND date <= :end")
    fun getTotalSalesByPisowifi(pisowifiId: Int, start: Long, end: Long): Flow<Double>

    @Query("""
        SELECT p.id AS pisowifiId, p.name AS deviceName, COALESCE(SUM(s.amount), 0) AS total
        FROM pisowifi_devices p
        LEFT JOIN sale_records s ON s.pisowifi_id = p.id AND s.date >= :start AND s.date <= :end
        GROUP BY p.id
        ORDER BY p.name ASC
    """)
    fun getSalesSummaryByPisowifi(start: Long, end: Long): Flow<List<PisowifiSalesSummary>>
}
