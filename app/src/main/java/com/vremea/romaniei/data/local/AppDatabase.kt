package com.vremea.romaniei.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vremea.romaniei.data.local.dao.AlertDao
import com.vremea.romaniei.data.local.dao.LocationDao
import com.vremea.romaniei.data.local.dao.WeatherDao
import com.vremea.romaniei.data.local.entity.AlertEntity
import com.vremea.romaniei.data.local.entity.LocationEntity
import com.vremea.romaniei.data.local.entity.WeatherEntity

@Database(
    entities = [WeatherEntity::class, AlertEntity::class, LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun alertDao(): AlertDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vremea_romaniei.db"
                )
                .fallbackToDestructiveMigration(false)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
