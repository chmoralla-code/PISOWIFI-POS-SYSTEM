package com.pisowifi.pos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pisowifi.pos.data.entity.PisowifiDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface PisowifiDao {
    @Query("SELECT * FROM pisowifi_devices ORDER BY name ASC")
    fun getAll(): Flow<List<PisowifiDevice>>

    @Query("SELECT * FROM pisowifi_devices WHERE id = :id")
    suspend fun getById(id: Int): PisowifiDevice?

    @Query("SELECT * FROM pisowifi_devices WHERE area_id = :areaId ORDER BY name ASC")
    fun getByArea(areaId: Int): Flow<List<PisowifiDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: PisowifiDevice): Long

    @Update
    suspend fun update(device: PisowifiDevice)

    @Delete
    suspend fun delete(device: PisowifiDevice)
}
