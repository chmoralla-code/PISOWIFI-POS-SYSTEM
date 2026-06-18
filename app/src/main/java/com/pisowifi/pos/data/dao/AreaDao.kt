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
}
