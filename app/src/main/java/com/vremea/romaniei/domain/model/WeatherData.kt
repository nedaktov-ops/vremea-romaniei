package com.vremea.romaniei.domain.model

data class WeatherData(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather?,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val alerts: List<AlertData>,
    val airQuality: AirQuality?,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val windGusts: Double,
    val cloudCover: Int,
    val visibility: Double,
    val uvIndex: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val isDay: Boolean
)

data class HourlyWeather(
    val time: Long,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val windGusts: Double,
    val precipitation: Double,
    val precipitationProbability: Int,
    val weatherCode: Int,
    val weatherIcon: String,
    val cloudCover: Int,
    val visibility: Double,
    val uvIndex: Double
)

data class DailyWeather(
    val date: Long,
    val tempMax: Double,
    val tempMin: Double,
    val feelsLikeMax: Double,
    val feelsLikeMin: Double,
    val sunrise: Long,
    val sunset: Long,
    val precipitationSum: Double,
    val precipitationProbabilityMax: Int,
    val weatherCode: Int,
    val weatherIcon: String,
    val windSpeedMax: Double,
    val windGustsMax: Double,
    val windDirectionDominant: Int,
    val humidityMean: Int? = null,
    val pressureMean: Double? = null,
    val uvIndexMax: Double,
    val description: String
)

data class AlertData(
    val id: String,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val eventType: String,
    val startTime: Long,
    val endTime: Long,
    val source: String,
    val locationNames: List<String>,
    val colorHex: String
)

enum class AlertSeverity {
    MINOR, MODERATE, SEVERE, EXTREME;

    /** RGB hex string (no leading #) for this severity — single source of truth. */
    fun toHexColor(): String = when (this) {
        MINOR -> "2196F3"
        MODERATE -> "FFC107"
        SEVERE -> "FF9800"
        EXTREME -> "F44336"
    }

    /** ARGB int for Android NotificationCompat.Builder.setColor(). */
    fun toNotificationColor(): Int = when (this) {
        MINOR -> 0xFF2196F3.toInt()
        MODERATE -> 0xFFFFC107.toInt()
        SEVERE -> 0xFFFF9800.toInt()
        EXTREME -> 0xFFF44336.toInt()
    }
}

data class AirQuality(
    val europeanAqi: Int,
    val usAqi: Int,
    val pm2_5: Double,
    val pm10: Double,
    val ozone: Double,
    val nitrogenDioxide: Double,
    val sulphurDioxide: Double,
    val carbonMonoxide: Double
)
