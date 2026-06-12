package com.vremea.romaniei.data.repository

import com.vremea.romaniei.data.remote.NetworkClient
import com.vremea.romaniei.data.remote.dto.OpenMeteoResponse
import com.vremea.romaniei.data.remote.dto.GeocodingResultDto
import com.vremea.romaniei.domain.model.*

class WeatherRepository {

    private val api = NetworkClient.openMeteoApi

    suspend fun getForecast(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> = runCatching {
        val response = api.getForecast(latitude, longitude)
        mapToWeatherData(response)
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

    @kotlinx.coroutines.ObsoleteCoroutinesApi
    private fun mapToAirQualityV2(response: OpenMeteoResponse): AirQuality? {
        // TODO: Air quality API returns different fields than weather forecast.
        // This function should be called against the air-quality endpoint response
        // which has european_aqi, us_aqi, pm2_5, pm10, etc. in the "current" block.
        // Currently blocked by: OpenMeteoResponse maps both forecast AND air-quality
        // through the same DTO. A separate AirQualityResponse DTO is needed.
        return null
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
        // Open-Meteo returns times in local timezone (requested via timezone=auto).
        // Romania is UTC+2 (winter) / UTC+3 (summer/EEST).
        // Try ISO-8601 with offset first, then local-without-offset assuming EET/EEST.
        return try {
            // If the string has a timezone offset (e.g. "2024-01-15T14:00:00+02:00")
            java.time.OffsetDateTime.parse(timeStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                .toInstant().toEpochMilli()
        } catch (_: Exception) {
            try {
                // No offset — assume Europe/Bucharest timezone
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
}
