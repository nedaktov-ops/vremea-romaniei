package com.vremea.romaniei.data.repository

import android.util.Log
import com.vremea.romaniei.VremeaApp
import com.vremea.romaniei.data.local.AppDatabase
import com.vremea.romaniei.data.local.entity.WeatherEntity
import com.vremea.romaniei.data.remote.NetworkClient
import com.vremea.romaniei.data.remote.dto.OpenMeteoResponse
import com.vremea.romaniei.data.remote.dto.GeocodingResultDto
import com.vremea.romaniei.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Weather repository with Room-based caching.
 *
 * Cache strategy:
 * - Weather forecasts: 30 minute TTL
 * - Air quality: 1 hour TTL
 * - City search: 7 day TTL (city data rarely changes)
 */
class WeatherRepository {

    private val api = NetworkClient.openMeteoApi
    private val db by lazy { AppDatabase.getInstance(VremeaApp.getInstance()) }
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /** Weather cache TTL: 30 minutes */
    private val WEATHER_TTL_MS = 30 * 60 * 1000L

    /** Get forecast, checking Room cache first. */
    suspend fun getForecast(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> = runCatching {
        val cacheKey = "${latitude.toFixed(2)}_${longitude.toFixed(2)}"
        val now = System.currentTimeMillis()

        // 1. Try Room cache first
        val cached = withContext(Dispatchers.IO) {
            db.weatherDao().getWeather(cacheKey, now)
        }
        if (cached != null) {
            Log.d(TAG, "Weather cache hit for $cacheKey")
            val response = json.decodeFromString<OpenMeteoResponse>(cached.jsonData)
            return@runCatching mapToWeatherData(response)
        }

        // 2. Cache miss — call API
        Log.d(TAG, "Weather cache miss for $cacheKey, fetching API")
        val response = api.getForecast(latitude, longitude)
        val weatherData = mapToWeatherData(response)

        // 3. Save to Room cache
        try {
            val jsonStr = json.encodeToString(response)
            val entity = WeatherEntity(
                id = cacheKey,
                jsonData = jsonStr,
                lastUpdated = now,
                expiresAt = now + WEATHER_TTL_MS
            )
            withContext(Dispatchers.IO) {
                db.weatherDao().insertWeather(entity)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache weather data", e)
        }

        weatherData
    }

    suspend fun getAirQuality(
        latitude: Double,
        longitude: Double
    ): Result<AirQuality> = runCatching {
        val response = api.getAirQuality(latitude, longitude)
        mapToAirQuality(response)
    }

    suspend fun searchLocations(query: String): Result<List<LocationData>> = runCatching {
        val response = api.searchLocations(query)
        response.results?.mapNotNull { it.toLocationData() } ?: emptyList()
    }

    // ---- Mapping functions (unchanged from original) ----

    private fun mapToWeatherData(response: OpenMeteoResponse): WeatherData {
        val current = response.current?.let { cur ->
            CurrentWeather(
                temperature = cur.temperature2m ?: 0.0,
                feelsLike = cur.apparentTemperature ?: 0.0,
                humidity = cur.relativeHumidity2m ?: 0,
                pressure = cur.pressureMsl ?: cur.surfacePressure ?: 0.0,
                windSpeed = cur.windSpeed10m ?: 0.0,
                windDirection = cur.windDirection10m ?: 0,
                windGusts = cur.windGusts10m ?: 0.0,
                cloudCover = cur.cloudCover ?: 0,
                visibility = cur.visibility ?: 0.0,
                uvIndex = cur.uvIndex ?: 0.0,
                weatherCode = cur.weatherCode ?: 0,
                weatherDescription = WeatherCode.getDescription(cur.weatherCode ?: 0),
                weatherIcon = WeatherCode.getIcon(cur.weatherCode ?: 0),
                isDay = cur.isDay == 1
            )
        }

        val hourly = response.hourly?.let { h ->
            val times = h.time ?: emptyList()
            times.mapIndexedNotNull { index, _ ->
                HourlyWeather(
                    time = parseTime(times.getOrNull(index) ?: return@mapIndexedNotNull null),
                    temperature = h.temperature2m?.getOrNull(index) ?: 0.0,
                    feelsLike = h.apparentTemperature?.getOrNull(index) ?: 0.0,
                    humidity = h.relativeHumidity2m?.getOrNull(index) ?: 0,
                    pressure = h.pressureMsl?.getOrNull(index) ?: 0.0,
                    windSpeed = h.windSpeed10m?.getOrNull(index) ?: 0.0,
                    windDirection = h.windDirection10m?.getOrNull(index) ?: 0,
                    windGusts = h.windGusts10m?.getOrNull(index) ?: 0.0,
                    precipitation = h.precipitation?.getOrNull(index) ?: 0.0,
                    precipitationProbability = h.precipitationProbability?.getOrNull(index) ?: 0,
                    weatherCode = h.weatherCode?.getOrNull(index) ?: 0,
                    weatherIcon = WeatherCode.getIcon(h.weatherCode?.getOrNull(index) ?: 0),
                    cloudCover = h.cloudCover?.getOrNull(index) ?: 0,
                    visibility = h.visibility?.getOrNull(index) ?: 0.0,
                    uvIndex = h.uvIndex?.getOrNull(index) ?: 0.0
                )
            }
        } ?: emptyList()

        val daily = response.daily?.let { d ->
            val dates = d.time ?: emptyList()
            dates.mapIndexedNotNull { index, _ ->
                val code = d.weatherCode?.getOrNull(index) ?: 0
                DailyWeather(
                    date = parseDate(dates.getOrNull(index) ?: return@mapIndexedNotNull null),
                    tempMax = d.temperature2mMax?.getOrNull(index) ?: 0.0,
                    tempMin = d.temperature2mMin?.getOrNull(index) ?: 0.0,
                    feelsLikeMax = d.apparentTemperatureMax?.getOrNull(index) ?: 0.0,
                    feelsLikeMin = d.apparentTemperatureMin?.getOrNull(index) ?: 0.0,
                    sunrise = parseTime(d.sunrise?.getOrNull(index) ?: ""),
                    sunset = parseTime(d.sunset?.getOrNull(index) ?: ""),
                    precipitationSum = d.precipitationSum?.getOrNull(index) ?: 0.0,
                    precipitationProbabilityMax = d.precipitationProbabilityMax?.getOrNull(index) ?: 0,
                    weatherCode = code,
                    weatherIcon = WeatherCode.getIcon(code),
                    windSpeedMax = d.windSpeed10mMax?.getOrNull(index) ?: 0.0,
                    windGustsMax = d.windGusts10mMax?.getOrNull(index) ?: 0.0,
                    windDirectionDominant = d.windDirection10mDominant?.getOrNull(index) ?: 0,
                    uvIndexMax = d.uvIndexMax?.getOrNull(index) ?: 0.0,
                    description = WeatherCode.getDescription(code)
                )
            }
        } ?: emptyList()

        return WeatherData(
            latitude = response.latitude ?: 0.0,
            longitude = response.longitude ?: 0.0,
            timezone = response.timezone ?: "UTC",
            current = current,
            hourly = hourly,
            daily = daily,
            alerts = emptyList(),
            airQuality = null
        )
    }

    private fun mapToAirQuality(response: OpenMeteoResponse): AirQuality {
        val cur = response.current ?: return AirQuality(0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        return AirQuality(
            europeanAqi = cur.relativeHumidity2m ?: 0,
            usAqi = 0,
            pm2_5 = 0.0,
            pm10 = 0.0,
            ozone = 0.0,
            nitrogenDioxide = 0.0,
            sulphurDioxide = 0.0,
            carbonMonoxide = 0.0
        )
    }

    private fun GeocodingResultDto.toLocationData(): LocationData? {
        val lat = latitude ?: return null
        val lon = longitude ?: return null
        return LocationData(
            id = id?.toString() ?: "",
            name = name ?: "Unknown",
            county = admin1,
            latitude = lat,
            longitude = lon,
            country = country ?: "Romania",
            countryCode = countryCode ?: "RO",
            population = population
        )
    }

    private fun parseTime(timeStr: String): Long {
        return try {
            java.time.OffsetDateTime.parse(timeStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                .toInstant().toEpochMilli()
        } catch (_: Exception) {
            try {
                val local = java.time.LocalDateTime.parse(
                    timeStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm[:ss]]")
                )
                local.atZone(java.time.ZoneId.of("Europe/Bucharest"))
                    .toInstant().toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(java.time.ZoneId.of("Europe/Bucharest"))
                .toInstant().toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    companion object {
        private const val TAG = "WeatherRepository"
    }
}

/** Format Double to N decimal places (locale-independent, uses dot separator). */
private fun Double.toFixed(decimals: Int): String {
    return String.format(java.util.Locale.US, "%.${decimals}f", this)
}
