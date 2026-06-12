package com.vremea.romaniei.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vremea.romaniei.data.local.entity.LocationEntity

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_cache WHERE name LIKE '%' || :query || '%' ORDER BY population DESC")
    suspend fun searchByName(query: String): List<LocationEntity>

    @Query("SELECT * FROM location_cache ORDER BY population DESC")
    suspend fun getAll(): List<LocationEntity>

    @Upsert
    suspend fun insertAll(locations: List<LocationEntity>)
}
