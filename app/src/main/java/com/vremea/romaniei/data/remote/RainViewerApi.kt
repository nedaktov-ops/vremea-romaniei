package com.vremea.romaniei.data.remote

import com.vremea.romaniei.data.remote.dto.RainViewerManifest
import retrofit2.http.GET

interface RainViewerApi {

    /** Fetch the radar frame manifest (list of past + nowcast frames). */
    @GET("public/weather-maps.json")
    suspend fun getManifest(): RainViewerManifest
}
