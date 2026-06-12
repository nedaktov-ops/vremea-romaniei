package com.vremea.romaniei.data.remote

import retrofit2.Response
import retrofit2.http.GET
import okhttp3.ResponseBody

interface AnmApi {
    @GET("wp-json/meteoapi/v2/avertizari-generale")
    suspend fun getGeneralWarnings(): Response<ResponseBody>

    @GET("wp-json/meteoapi/v2/avertizari-nowcasting")
    suspend fun getNowcastingWarnings(): Response<ResponseBody>

    @GET("wp-json/meteoapi/v2/prognoza-orase")
    suspend fun getCityForecasts(): Response<ResponseBody>
}

// MeteoAlarm legacy Atom feed: returns XML with <feed><entry> entries.
// The suffix after the base URL is the country code.
// Romania = "ro" → full path: feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-romania
interface MeteoAlarmApi {
    @GET("feeds/meteoalarm-legacy-atom-romania")
    suspend fun getAlerts(): Response<ResponseBody>
}

interface RainViewerApi {
    @GET("public/weather-maps.json")
    suspend fun getRadarManifest(): okhttp3.ResponseBody
}
