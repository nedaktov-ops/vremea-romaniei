package com.vremea.romaniei.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vremea.romaniei.data.local.entity.AlertEntity

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_cache")
    suspend fun getAll(): List<AlertEntity>

    @Query("DELETE FROM alert_cache")
    suspend fun deleteAll()

    @Upsert
    suspend fun insertAlerts(alerts: List<AlertEntity>)
}
