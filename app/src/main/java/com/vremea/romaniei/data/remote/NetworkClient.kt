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
        level = HttpLoggingInterceptor.Level.BODY
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

    private val meteoAlarmRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://meteoalarm-legacy-atom-romania/")
        .client(okHttpClient)
        .build()

    private val rainViewerRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.rainviewer.com/")
        .client(okHttpClient)
        .build()

    val openMeteoApi: OpenMeteoApi = retrofit.create(OpenMeteoApi::class.java)
    val anmApi: AnmApi = anmRetrofit.create(AnmApi::class.java)
    val meteoAlarmApi: MeteoAlarmApi = meteoAlarmRetrofit.create(MeteoAlarmApi::class.java)
    val rainViewerApi: RainViewerApi = rainViewerRetrofit.create(RainViewerApi::class.java)
}
