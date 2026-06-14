package com.vremea.romaniei.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vremea.romaniei.data.local.dao.WeatherDao
import com.vremea.romaniei.data.local.entity.WeatherEntity

@Database(
    entities = [WeatherEntity::class],
    version = 2,
    exportSchema = false  // Set to true when Room compiler matches kotlinx-serialization version
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vremea_romaniei.db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
