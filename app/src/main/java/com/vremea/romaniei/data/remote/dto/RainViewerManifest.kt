package com.vremea.romaniei.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RainViewerManifest(
    val version: String = "",
    val generated: Long = 0,
    val host: String = "",
    val radar: RadarData = RadarData()
)

@Serializable
data class RadarData(
    val past: List<RadarFrame> = emptyList(),
    val nowcast: List<RadarFrame> = emptyList()
)

@Serializable
data class RadarFrame(
    val time: Long = 0,
    val path: String = ""
)
