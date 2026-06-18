package com.pisowifi.pos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pisowifi.pos.data.dao.AreaDao
import com.pisowifi.pos.data.dao.HarvestDao
import com.pisowifi.pos.data.dao.PisowifiDao
import com.pisowifi.pos.data.dao.SaleDao
import com.pisowifi.pos.data.entity.Area
import com.pisowifi.pos.data.entity.HarvestRecord
import com.pisowifi.pos.data.entity.PisowifiDevice
import com.pisowifi.pos.data.entity.SaleRecord

@Database(
    entities = [Area::class, PisowifiDevice::class, SaleRecord::class, HarvestRecord::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
    abstract fun pisowifiDao(): PisowifiDao
    abstract fun saleDao(): SaleDao
    abstract fun harvestDao(): HarvestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pisowifi_pos_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
