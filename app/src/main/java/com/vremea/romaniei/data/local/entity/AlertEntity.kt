package com.vremea.romaniei.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_cache")
data class AlertEntity(
    @PrimaryKey val id: String,
    val jsonData: String,
    val lastUpdated: Long
)
