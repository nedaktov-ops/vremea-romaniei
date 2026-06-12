package com.vremea.romaniei.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vremea.romaniei.data.local.entity.WeatherEntity

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE id = :id AND expiresAt > :now")
    suspend fun getWeather(id: String, now: Long): WeatherEntity?

    @Upsert
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather_cache WHERE expiresAt < :now")
    suspend fun deleteExpired(now: Long)
}
