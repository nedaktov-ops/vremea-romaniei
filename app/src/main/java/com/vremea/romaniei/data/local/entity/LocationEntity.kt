package com.vremea.romaniei.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_cache")
data class LocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val county: String?,
    val latitude: Double,
    val longitude: Double,
    val population: Int?
)
