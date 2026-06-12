package com.vremea.romaniei.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val id: String,
    val jsonData: String,
    val lastUpdated: Long,
    val expiresAt: Long
)
