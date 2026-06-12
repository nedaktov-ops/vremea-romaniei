package com.vremea.romaniei.domain.model

fun Int.toWindDirection(): String {
    return when (this) {
        in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SW"
        in 248..292 -> "W"
        in 293..337 -> "NW"
        else -> "N"
    }
}

fun Int.toWindDirectionRo(): String {
    return when (this) {
        in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SV"
        in 248..292 -> "V"
        in 293..337 -> "NV"
        else -> "N"
    }
}

fun Double.toPressure(): String {
    return "%.1f".format(this)
}

fun Int.toVisibility(): String {
    return when {
        this < 1000 -> "${this}m"
        else -> "%.1fkm".format(this / 1000.0)
    }
}

fun Double.toKmh(): String {
    return "%.0f km/h".format(this)
}

fun Int.toUvDescription(): String {
    return when {
        this <= 2 -> "Low"
        this <= 5 -> "Moderate"
        this <= 7 -> "High"
        this <= 10 -> "Very High"
        else -> "Extreme"
    }
}

fun Int.toUvDescriptionRo(): String {
    return when {
        this <= 2 -> "Scăzut"
        this <= 5 -> "Moderat"
        this <= 7 -> "Ridicat"
        this <= 10 -> "Foarte Ridicat"
        else -> "Extrem"
    }
}
