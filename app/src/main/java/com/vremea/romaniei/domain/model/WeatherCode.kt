package com.vremea.romaniei.domain.model

object WeatherCode {

    fun getDescription(code: Int, ro: Boolean = java.util.Locale.getDefault().language == "ro"): String {
        val lang = if (ro) roDescriptions else enDescriptions
        return lang[code] ?: (if (ro) "Necunoscut" else "Unknown")
    }

    fun getIcon(code: Int): String {
        return when (code) {
            0 -> "sunny"
            1 -> "mostly_sunny"
            2 -> "partly_cloudy"
            3 -> "overcast"
            45, 48 -> "foggy"
            51, 53, 55 -> "drizzle"
            56, 57 -> "freezing_drizzle"
            61, 63, 65 -> "rainy"
            66, 67 -> "freezing_rain"
            71, 73, 75 -> "snowy"
            77 -> "snow_grains"
            80, 81, 82 -> "rain_showers"
            85, 86 -> "snow_showers"
            95 -> "thunderstorm"
            96, 99 -> "thunderstorm_hail"
            else -> "unknown"
        }
    }

    private val enDescriptions = mapOf(
        0 to "Clear Sky",
        1 to "Mainly Clear",
        2 to "Partly Cloudy",
        3 to "Overcast",
        45 to "Foggy",
        48 to "Depositing Rime Fog",
        51 to "Light Drizzle",
        53 to "Moderate Drizzle",
        55 to "Dense Drizzle",
        56 to "Light Freezing Drizzle",
        57 to "Dense Freezing Drizzle",
        61 to "Slight Rain",
        63 to "Moderate Rain",
        65 to "Heavy Rain",
        66 to "Light Freezing Rain",
        67 to "Heavy Freezing Rain",
        71 to "Slight Snow",
        73 to "Moderate Snow",
        75 to "Heavy Snow",
        77 to "Snow Grains",
        80 to "Slight Rain Showers",
        81 to "Moderate Rain Showers",
        82 to "Violent Rain Showers",
        85 to "Slight Snow Showers",
        86 to "Heavy Snow Showers",
        95 to "Thunderstorm",
        96 to "Thunderstorm with Hail",
        99 to "Thunderstorm with Heavy Hail"
    )

    private val roDescriptions = mapOf(
        0 to "Cer Senin",
        1 to "Predominant Senin",
        2 to "Parțial Noros",
        3 to "Acoperit",
        45 to "Cețos",
        48 to "Chiciură",
        51 to "Burniță Ușoară",
        53 to "Burniță Moderată",
        55 to "Burniță Densoasă",
        56 to "Burniță Ușoară Înghețată",
        57 to "Burniță Densoasă Înghețată",
        61 to "Ploaie Ușoară",
        63 to "Ploaie Moderată",
        65 to "Ploaie Torențială",
        66 to "Ploaie Înghețată Ușoară",
        67 to "Ploaie Înghețată Torențială",
        71 to "Ninsoare Ușoară",
        73 to "Ninsoare Moderată",
        75 to "Ninsoare Viscolită",
        77 to "Grăunțe de Zăpadă",
        80 to "Averse Ușoare de Ploaie",
        81 to "Averse Moderate de Ploaie",
        82 to "Averse Violente de Ploaie",
        85 to "Averse Ușoare de Zăpadă",
        86 to "Averse Puternice de Zăpadă",
        95 to "Furtună",
        96 to "Furtună cu Grindină",
        99 to "Furtună cu Grindină Mare"
    )
}
