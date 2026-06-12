package com.vremea.romaniei.domain.model

data class LocationData(
    val id: String = "",
    val name: String,
    val county: String? = null,
    val region: String? = null,
    val latitude: Double,
    val longitude: Double,
    val country: String = "Romania",
    val countryCode: String = "RO",
    val isFavorite: Boolean = false,
    val population: Int? = null
)
