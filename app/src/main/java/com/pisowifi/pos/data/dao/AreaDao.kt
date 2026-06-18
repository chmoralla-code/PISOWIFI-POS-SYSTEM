package com.pisowifi.pos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pisowifi.pos.data.entity.Area
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas ORDER BY name ASC")
    fun getAll(): Flow<List<Area>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(area: Area): Long

    @Update
    suspend fun update(area: Area)

    @Delete
    suspend fun delete(area: Area)

    @Query("UPDATE areas SET share_given = :given, last_shared_date = :date WHERE id = :id")
    suspend fun updateShareStatus(id: Int, given: Boolean, date: Long = System.currentTimeMillis())

    @Query("SELECT * FROM areas WHERE share_given = 0 ORDER BY name ASC")
    fun getPendingShares(): Flow<List<Area>>

    @Query("SELECT * FROM areas WHERE share_given = 1 ORDER BY last_shared_date DESC")
    fun getCompletedShares(): Flow<List<Area>>

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM sale_records s INNER JOIN pisowifi_devices p ON s.pisowifi_id = p.id WHERE p.area_id = :areaId")
    suspend fun getTotalSalesByArea(areaId: Int): Double

    @Query("SELECT COALESCE(SUM(h.share_amount), 0) FROM harvest_records h INNER JOIN pisowifi_devices p ON h.pisowifi_id = p.id WHERE p.area_id = :areaId")
    suspend fun getTotalHarvestShareByArea(areaId: Int): Double
}
