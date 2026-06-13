package com.vremea.romaniei.data.remote

import com.vremea.romaniei.data.remote.dto.OpenMeteoResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

object NetworkClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Only log headers in release; full body in debug
        level = if (com.vremea.romaniei.BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val anmRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.meteoromania.ro/")
        .client(okHttpClient)
        .build()

    // MeteoAlarm: feed URL varies by country.
    // For Romania (ro), the legacy Atom feed is at:
    // https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-romania
    // But this RSS/XML feed requires parsing. The correct base + path
    // combination is set here; the MeteoAlarmApi interface must match.
    // As of 2025, the working Atom URL is:
    // https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-romania
    private val meteoAlarmRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://feeds.meteoalarm.org/")
        .client(okHttpClient)
        .build()

    private val rainViewerRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.rainviewer.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val openMeteoApi: OpenMeteoApi = retrofit.create(OpenMeteoApi::class.java)
    val anmApi: AnmApi = anmRetrofit.create(AnmApi::class.java)
    val meteoAlarmApi: MeteoAlarmApi = meteoAlarmRetrofit.create(MeteoAlarmApi::class.java)
    val rainViewerApi: RainViewerApi = rainViewerRetrofit.create(RainViewerApi::class.java)
}
