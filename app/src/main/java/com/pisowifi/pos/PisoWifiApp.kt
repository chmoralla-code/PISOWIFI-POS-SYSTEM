package com.pisowifi.pos

import android.app.Application
import com.pisowifi.pos.data.database.AppDatabase
import com.pisowifi.pos.data.repository.PisoRepository

class PisoWifiApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { PisoRepository(database) }
}
