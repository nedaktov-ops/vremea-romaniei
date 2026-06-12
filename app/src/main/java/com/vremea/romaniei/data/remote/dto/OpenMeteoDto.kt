package com.vremea.romaniei.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val current: CurrentWeatherDto? = null,
    val hourly: HourlyDataDto? = null,
    val daily: DailyDataDto? = null,
    val results: List<GeocodingResultDto>? = null
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("time") val time: String? = null,
    @SerialName("interval") val interval: Long? = null,
    @SerialName("temperature_2m") val temperature2m: Double? = null,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @SerialName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
    @SerialName("cloud_cover") val cloudCover: Int? = null,
    @SerialName("pressure_msl") val pressureMsl: Double? = null,
    @SerialName("surface_pressure") val surfacePressure: Double? = null,
    @SerialName("wind_speed_10m") val windSpeed10m: Double? = null,
    @SerialName("wind_direction_10m") val windDirection10m: Int? = null,
    @SerialName("wind_gusts_10m") val windGusts10m: Double? = null,
    @SerialName("visibility") val visibility: Double? = null,
    @SerialName("uv_index") val uvIndex: Double? = null,
    @SerialName("is_day") val isDay: Int? = null
)

@Serializable
data class HourlyDataDto(
    val time: List<String>? = null,
    @SerialName("temperature_2m") val temperature2m: List<Double>? = null,
    @SerialName("apparent_temperature") val apparentTemperature: List<Double>? = null,
    @SerialName("precipitation") val precipitation: List<Double>? = null,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int>? = null,
    @SerialName("weather_code") val weatherCode: List<Int>? = null,
    @SerialName("cloud_cover") val cloudCover: List<Int>? = null,
    @SerialName("pressure_msl") val pressureMsl: List<Double>? = null,
    @SerialName("wind_speed_10m") val windSpeed10m: List<Double>? = null,
    @SerialName("wind_direction_10m") val windDirection10m: List<Int>? = null,
    @SerialName("wind_gusts_10m") val windGusts10m: List<Double>? = null,
    @SerialName("visibility") val visibility: List<Double>? = null,
    @SerialName("uv_index") val uvIndex: List<Double>? = null,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: List<Int>? = null
)

@Serializable
data class DailyDataDto(
    val time: List<String>? = null,
    @SerialName("weather_code") val weatherCode: List<Int>? = null,
    @SerialName("temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @SerialName("temperature_2m_min") val temperature2mMin: List<Double>? = null,
    @SerialName("apparent_temperature_max") val apparentTemperatureMax: List<Double>? = null,
    @SerialName("apparent_temperature_min") val apparentTemperatureMin: List<Double>? = null,
    @SerialName("sunrise") val sunrise: List<String>? = null,
    @SerialName("sunset") val sunset: List<String>? = null,
    @SerialName("precipitation_sum") val precipitationSum: List<Double>? = null,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null,
    @SerialName("wind_speed_10m_max") val windSpeed10mMax: List<Double>? = null,
    @SerialName("wind_gusts_10m_max") val windGusts10mMax: List<Double>? = null,
    @SerialName("wind_direction_10m_dominant") val windDirection10mDominant: List<Int>? = null,
    @SerialName("uv_index_max") val uvIndexMax: List<Double>? = null
)

@Serializable
data class GeocodingResultDto(
    val id: Long? = null,
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("admin1") val admin1: String? = null,
    val population: Int? = null
)

