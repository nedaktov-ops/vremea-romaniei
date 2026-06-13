package com.vremea.romaniei.data.repository

import com.vremea.romaniei.data.remote.NetworkClient
import com.vremea.romaniei.data.remote.dto.RadarFrame

/**
 * Fetches and caches the RainViewer radar frame manifest.
 *
 * Frame URL format: {host}{path}/256/{z}/{x}/{y}.png
 * e.g. https://tilecache.rainviewer.com/v2/radar/8985e4415689/256/{z}/{x}/{y}.png
 */
class RainViewerRepository {

    private val api = NetworkClient.rainViewerApi

    /** Cached manifest — avoid refetching on every map render. */
    private var cachedManifest: RainViewerManifestWrapper? = null

    /** Fetch manifest, or return cached if still fresh (< 5 min). */
    suspend fun getFrames(refresh: Boolean = false): RainViewerManifestWrapper {
        val now = System.currentTimeMillis() / 1000L
        val cached = cachedManifest
        if (!refresh && cached != null && (now - cached.fetchedAt) < 300) {
            return cached
        }
        val manifest = api.getManifest()
        val wrapper = RainViewerManifestWrapper(
            host = manifest.host,
            allFrames = manifest.radar.past + manifest.radar.nowcast,
            fetchedAt = now
        )
        cachedManifest = wrapper
        return wrapper
    }

    /** Build a tile URL template for a given frame. */
    fun buildTileUrl(host: String, path: String): String {
        return "$host${path}/256/{z}/{x}/{y}.png"
    }
}

data class RainViewerManifestWrapper(
    val host: String,
    val allFrames: List<RadarFrame>,
    val fetchedAt: Long
) {
    /** Number of frames available for animation. */
    val frameCount: Int get() = allFrames.size

    /** Get frame at index, wrapping around if out of bounds. */
    fun getFrame(index: Int): RadarFrame {
        return allFrames[index.coerceIn(0, allFrames.lastIndex)]
    }
}
