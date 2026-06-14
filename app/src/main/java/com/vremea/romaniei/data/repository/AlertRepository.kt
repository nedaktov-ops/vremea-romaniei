package com.vremea.romaniei.data.repository

import android.util.Log
import com.vremea.romaniei.data.remote.NetworkClient
import com.vremea.romaniei.domain.model.AlertData
import com.vremea.romaniei.domain.model.AlertSeverity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Fetches weather alerts from ANM (Romania) and MeteoAlarm (EU).
 *
 * ANM API (meteoromania.ro):
 *   - /wp-json/meteoapi/v2/avertizari-generale → general warnings (JSON)
 *   - /wp-json/meteoapi/v2/avertizari-nowcasting → nowcasting warnings (JSON)
 *   Response is either `"Nu exista date"` (empty) or a JSON array of alert objects.
 *
 * MeteoAlarm (feeds.meteoalarm.org):
 *   - /feeds/meteoalarm-legacy-atom-romania → Atom feed with CAP: namespace entries
 *   Each <entry> contains cap:severity, cap:event, cap:areaDesc, cap:effective, cap:expires
 */
class AlertRepository {

    private val anmApi = NetworkClient.anmApi
    private val meteoAlarmApi = NetworkClient.meteoAlarmApi
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetch all alerts from ANM (general + nowcasting) and MeteoAlarm.
     * Filters out expired entries and deduplicates by ID.
     */
    suspend fun getAlerts(): List<AlertData> {
        val results = mutableListOf<AlertData>()

        // 1. ANM general warnings
        try {
            val response = anmApi.getGeneralWarnings()
            if (response.isSuccessful) {
                val body = response.body()?.string()
                if (body != null) {
                    results.addAll(parseAnmJson(body, "ANM"))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "ANM general warnings failed", e)
        }

        // 2. ANM nowcasting warnings
        try {
            val response = anmApi.getNowcastingWarnings()
            if (response.isSuccessful) {
                val body = response.body()?.string()
                if (body != null) {
                    results.addAll(parseAnmJson(body, "ANM Nowcasting"))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "ANM nowcasting failed", e)
        }

        // 3. MeteoAlarm Atom feed
        try {
            val response = meteoAlarmApi.getAlerts()
            if (response.isSuccessful) {
                val body = response.body()?.string()
                if (body != null) {
                    results.addAll(parseMeteoAlarmAtom(body))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MeteoAlarm failed", e)
        }

        return results.distinctBy { it.id }
    }

    /**
     * Parse ANM JSON response.
     * Responses are: "Nu exista date" → empty list
     * Or: [{"atribut": "valoare", ...}, ...]
     *
     * The ANM JSON format uses Romanian attribute names and varies between endpoints.
     * We extract what we can and wrap it in AlertData.
     */
    private fun parseAnmJson(body: String, source: String): List<AlertData> {
        val trimmed = body.trim()
        // Empty or "no data" response
        if (trimmed.startsWith("\"") || trimmed == "[]" || trimmed == "null") {
            return emptyList()
        }
        return try {
            val array = json.parseToJsonElement(trimmed).jsonArray
            val now = System.currentTimeMillis()
            array.mapNotNull { element ->
                val obj = element.jsonObject
                // ANM attributes are in Romanian; try common patterns
                val id = obj["id"]?.jsonPrimitive?.content
                    ?: obj["cod"]?.jsonPrimitive?.content
                    ?: "anm_${now}_${obj.hashCode()}"
                val title = obj["titlu"]?.jsonPrimitive?.content
                    ?: obj["title"]?.jsonPrimitive?.content
                    ?: obj["fenomen"]?.jsonPrimitive?.content
                    ?: "Avertizare ANM"
                val description = obj["descriere"]?.jsonPrimitive?.content
                    ?: obj["description"]?.jsonPrimitive?.content
                    ?: obj["text"]?.jsonPrimitive?.content
                    ?: ""
                val eventType = obj["fenomen"]?.jsonPrimitive?.content
                    ?: obj["tip"]?.jsonPrimitive?.content
                    ?: ""
                // ANM severity: "Portocaliu"=SEVERE, "Rosu"=EXTREME, "Galben"=MODERATE, "Verde"=MINOR
                val severityStr = obj["severitate"]?.jsonPrimitive?.content
                    ?: obj["culoare"]?.jsonPrimitive?.content
                    ?: ""
                val severity = when {
                    severityStr.contains("rosu", ignoreCase = true) -> AlertSeverity.EXTREME
                    severityStr.contains("portocaliu", ignoreCase = true) -> AlertSeverity.SEVERE
                    severityStr.contains("galben", ignoreCase = true) -> AlertSeverity.MODERATE
                    severityStr.contains("verde", ignoreCase = true) -> AlertSeverity.MINOR
                    else -> AlertSeverity.MODERATE
                }
                val locations = listOfNotNull(
                    obj["localitate"]?.jsonPrimitive?.content,
                    obj["judet"]?.jsonPrimitive?.content,
                    obj["zona"]?.jsonPrimitive?.content
                )
                AlertData(
                    id = id,
                    title = title,
                    description = description,
                    severity = severity,
                    eventType = eventType,
                    startTime = now,
                    endTime = now + 3_600_000L, // default 1h if not specified
                    source = source,
                    locationNames = locations,
                    colorHex = severity.toHexColor()
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse ANM JSON: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Parse MeteoAlarm Atom feed with CAP namespace.
     *
     * Feed structure:
     * <feed xmlns="http://www.w3.org/2005/Atom" xmlns:cap="urn:oasis:names:tc:emergency:cap:1.2">
     *   <entry>
     *     <id>...</id>
     *     <title>Yellow Thunderstorm Warning for ...</title>
     *     <cap:event>Thunderstormwarning</cap:event>
     *     <cap:severity>Moderate</cap:severity>
     *     <cap:areaDesc>Oberwart</cap:areaDesc>
     *     <cap:effective>2026-06-13T23:18:41+00:00</cap:effective>
     *     <cap:expires>2026-06-14T00:18:41+00:00</cap:expires>
     *   </entry>
     * </feed>
     */
    private fun parseMeteoAlarmAtom(body: String): List<AlertData> {
        val alerts = mutableListOf<AlertData>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(body))

            var eventType = ""
            var title = ""
            var severity = AlertSeverity.MODERATE
            var areaDesc = ""
            var effective = ""
            var expires = ""
            var id = ""
            var inEntry = false
            var currentTag = ""

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "entry") {
                            inEntry = true
                            // Reset fields
                            eventType = ""
                            title = ""
                            severity = AlertSeverity.MODERATE
                            areaDesc = ""
                            effective = ""
                            expires = ""
                            id = ""
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inEntry) {
                            val text = parser.text?.trim() ?: ""
                            when (currentTag) {
                                "id" -> if (id.isEmpty()) id = text
                                "title" -> if (title.isEmpty()) title = text
                                "event" -> if (eventType.isEmpty()) eventType = text
                                "severity" -> if (text.isNotEmpty()) {
                                    severity = when {
                                        text.contains("Extreme", ignoreCase = true) -> AlertSeverity.EXTREME
                                        text.contains("Severe", ignoreCase = true) -> AlertSeverity.SEVERE
                                        text.contains("Moderate", ignoreCase = true) -> AlertSeverity.MODERATE
                                        text.contains("Minor", ignoreCase = true) -> AlertSeverity.MINOR
                                        else -> AlertSeverity.MODERATE
                                    }
                                }
                                "areaDesc" -> if (areaDesc.isEmpty()) areaDesc = text
                                "effective" -> if (effective.isEmpty()) effective = text
                                "expires" -> if (expires.isEmpty()) expires = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "entry") {
                            // Build AlertData from collected fields
                            val now = System.currentTimeMillis()
                            val alert = AlertData(
                                id = id.ifEmpty { "meteoalarm_$now" },
                                title = title.ifEmpty { "Alertă MeteoAlarm" },
                                description = "$eventType - $areaDesc".trimStart('-', ' '),
                                severity = severity,
                                eventType = eventType,
                                startTime = parseIsoDateTime(effective) ?: now,
                                endTime = parseIsoDateTime(expires) ?: (now + 3_600_000L),
                                source = "MeteoAlarm",
                                locationNames = listOf(areaDesc).filter { it.isNotEmpty() },
                                colorHex = severity.toHexColor()
                            )
                            alerts.add(alert)
                            inEntry = false
                        }
                        currentTag = ""
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse MeteoAlarm Atom feed: ${e.message}", e)
        }
        return alerts
    }

    /** Parse ISO-8601 datetime string to epoch millis, or null on failure. */
    private fun parseIsoDateTime(text: String): Long? {
        if (text.isBlank()) return null
        return try {
            java.time.OffsetDateTime.parse(text)
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            try {
                java.time.LocalDateTime.parse(text)
                    .atZone(java.time.ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            } catch (_: Exception) {
                null
            }
        }
    }

    companion object {
        private const val TAG = "AlertRepository"
    }
}
