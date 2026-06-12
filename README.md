# VremeaRomâniei — God Mode Android Weather App

> **100% free, self-sustaining, privacy-first weather app for Romania.**
> Open-Meteo + ANM + MeteoAlarm EU + RainViewer. Offline cache. Background alerts. Interactive radar map.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Data Sources & APIs](#3-data-sources--apis)
4. [Build System](#4-build-system)
5. [Project Structure](#5-project-structure)
6. [Data Layer](#6-data-layer)
7. [Domain Layer](#7-domain-layer)
8. [UI Layer](#8-ui-layer)
9. [Notification System](#9-notification-system)
10. [Map & Radar Features](#10-map--radar-features)
11. [Offline Mode](#11-offline-mode)
12. [Localization (i18n)](#12-localization-i18n)
13. [Testing Strategy](#13-testing-strategy)
14. [CI/CD Pipeline](#14-cicd-pipeline)
15. [Distribution](#15-distribution)
16. [Development Setup](#16-development-setup)
17. [Build Scripts](#17-build-scripts)
18. [Troubleshooting](#18-troubleshooting)
19. [License & Attribution](#19-license--attribution)
20. [Key Decisions & Rationale](#20-key-decisions--rationale)

---

## 1. Project Overview

**VremeaRomâniei** is a native Android weather application built with Kotlin and Jetpack Compose, specifically designed for Romania. It aggregates weather data from four free sources to provide:

- **Current weather**: Temperature, humidity, wind, pressure, UV index, visibility, cloud cover
- **Hourly forecast**: 24-hour detailed breakdown with precipitation probability
- **16-day forecast**: Daily min/max temperatures, precipitation, sunrise/sunset
- **Romanian weather warnings**: ANM (National Meteorological Administration) official alerts
- **EU weather alerts**: MeteoAlarm cross-border warnings
- **Interactive radar map**: Animated RainViewer radar overlay on MapLibre map
- **Background notifications**: Automatic severe weather alerts via WorkManager
- **Full offline support**: Room database caching with TTL-based invalidation
- **Romanian + English UI**: Auto-detect or manual language switch

### Core Philosophy

| Principle | Implementation |
|-----------|---------------|
| **100% Free** | No API keys, no paid hosting, no Firebase, no paid distribution |
| **Privacy-First** | No analytics, no tracking, no user data collection |
| **Offline-First** | Room cache with graceful degradation |
| **Production Quality** | Tests, CI/CD, signed releases, documented |
| **Romanian Focused** | ANM integration, Romanian locale, Stereo 70 projection handling |

---

## 2. Architecture

### Pattern: MVVM + Repository + Manual DI

```
┌──────────────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │  Home    │ │ Forecast │ │   Map    │ │  Alerts  │ │ Settings │  │
│  │  Screen  │ │  Screen  │ │  Screen  │ │  Screen  │ │  Screen  │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
│       │            │            │            │            │         │
│  ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐  │
│  │  Home    │ │ Forecast │ │  Map     │ │ Alerts   │ │ Settings │  │
│  │ ViewModel│ │ ViewModel│ │ ViewModel│ │ViewModel  │ │ViewModel │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
└───────┼─────────────┼────────────┼────────────┼────────────┼────────┘
        │             │            │            │            │
┌───────▼─────────────▼────────────▼────────────▼────────────▼────────┐
│                        Domain Layer (Models)                         │
│  WeatherData, CurrentWeather, HourlyWeather, DailyWeather,          │
│  AlertData, AlertSeverity, AirQuality, LocationData, WeatherCode    │
└─────────────────────────────────────────────────────────────────────┘
        │             │            │            │
┌───────▼─────────────▼────────────▼────────────▼─────────────────────┐
│                        Data Layer (Repository)                       │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────────────┐ │
│  │ WeatherRepo    │  │  LocationRepo  │  │  AlertRepository       │ │
│  │ (forecast +    │  │  (city search) │  │  (ANM + MeteoAlarm)   │ │
│  │  air quality)  │  │                │  │                        │ │
│  └───────┬────────┘  └───────┬────────┘  └──────────┬─────────────┘ │
│          │                   │                      │                │
│  ┌───────▼────────┐  ┌──────▼───────┐  ┌───────────▼─────────────┐ │
│  │  Remote Data   │  │  Local Data  │  │  Remote Alert APIs      │ │
│  │  OpenMeteoApi  │  │  Room DB     │  │  ANMApi, MeteoAlarmApi  │ │
│  │  (Retrofit)    │  │  (SQLite)    │  │  RainViewerApi          │ │
│  └────────────────┘  └──────────────┘  └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### Dependency Flow (always downward)

```
UI Layer (Screens) → ViewModels → Domain Models → Repositories → Data Sources
                                                         ↕
                                                    Room Database
```

### Key Architectural Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **DI Approach** | Manual (no Hilt/Koin) | Simpler for single-dev project, no annotation processing overhead, full control |
| **Navigation** | Navigation Compose | Official solution, type-safe, deep link support |
| **State Management** | StateFlow + MutableStateFlow | Lifecycle-aware, clean unidirectional data flow |
| **Async** | Kotlin Coroutines + Flow | Native Kotlin, structured concurrency |
| **Project Structure** | Feature-first packages | Each feature (home, forecast, map, alerts, settings) is self-contained |

---

## 3. Data Sources & APIs

### 3.1 Open-Meteo (Primary Weather Data)

| Detail | Value |
|--------|-------|
| **URL** | `https://api.open-meteo.com/v1/forecast` |
| **Auth** | None required |
| **Rate Limits** | 600 calls/min, 5,000/hour, 10,000 calls/day |
| **License** | CC BY 4.0 (attribution required) |
| **Usage** | Current weather, hourly forecast, 16-day daily forecast, air quality |
| **Endpoints** | `/v1/forecast` (weather), `/v1/air-quality`, `/v1/geocoding` (city search) |
| **Params Used** | `current`, `hourly`, `daily`, `timezone=auto`, `forecast_days=16` |

**Requested fields:**
- **Current**: temperature_2m, relative_humidity_2m, apparent_temperature, weather_code, cloud_cover, pressure_msl, surface_pressure, wind_speed_10m, wind_direction_10m, wind_gusts_10m, visibility, uv_index, is_day
- **Hourly**: temperature_2m, apparent_temperature, precipitation, precipitation_probability, weather_code, cloud_cover, pressure_msl, wind_speed_10m, wind_direction_10m, wind_gusts_10m, visibility, uv_index, relative_humidity_2m
- **Daily**: weather_code, temperature_2m_max, temperature_2m_min, apparent_temperature_max, apparent_temperature_min, sunrise, sunset, precipitation_sum, precipitation_probability_max, wind_speed_10m_max, wind_gusts_10m_max, wind_direction_10m_dominant, uv_index_max
- **Geocoding**: name, count=10, language=ro, format=json

**Attribution**: Data provided by Open-Meteo.com (link required in app)

### 3.2 ANM — National Meteorological Administration (Romanian Warnings)

| Detail | Value |
|--------|-------|
| **Base URL** | `https://www.meteoromania.ro/wp-json/meteoapi/v2/` |
| **Auth** | None required |
| **License** | Public sector information (free use) |
| **Status** | ✅ Confirmed live (200 OK, 2026-06-12) |
| **Format** | JSON (not RSS/XML) |

**Available endpoints:**

| Endpoint | Description |
|----------|-------------|
| `starea-vremii` | Current weather conditions from stations (GeoJSON FeatureCollection) |
| `prognoza-orase` | 5-day forecast for 10 major cities |
| `avertizari-generale` | General weather warnings (cod galben/portocaliu/rosu) |
| `avertizari-nowcasting` | Immediate-term nowcasting warnings |

**⚠️ CRITICAL: Coordinate Projection**
ANM GeoJSON coordinates are **Stereo 70 (EPSG:31700)**, NOT WGS84 (EPSG:4326). They cannot be plotted directly on a standard map. The app uses Open-Meteo geocoding for location selection instead of ANM station data.

### 3.3 MeteoAlarm EU (European Weather Alerts)

| Detail | Value |
|--------|-------|
| **URL** | `https://meteoalarm-legacy-atom-romania` (Atom XML feed) |
| **Auth** | None required |
| **License** | CC BY 4.0 equivalent |
| **Format** | Atom XML with CAP:Alert entries |
| **Status** | ✅ New endpoint confirmed live |
| **⚠️ Old endpoint dead** | `feeds.meteoalarm.org/feeds/ro.json` returns 404 |

**Parse approach:** XML pull parser for Atom feed, extract CAP alert properties (event, severity, headline, description, onset, expires, areaDesc).

### 3.4 RainViewer (Radar Overlay)

| Detail | Value |
|--------|-------|
| **URL** | `https://api.rainviewer.com/public/weather-maps.json` (manifest) |
| **Tile URL** | `https://tilecache.rainviewer.com/v2/radar/{timestamp}/256/{zoom}/{x}/{y}/{format}.png` |
| **Auth** | None required |
| **License** | Free for non-commercial use |
| **Status** | ✅ Confirmed live |

**Manifest structure:**
- `past[]` — Historical radar frames, each with `time` (Unix timestamp), `path`, `type`
- `nowcast[]` — Forecast radar frames (next 90 min)
- Tile format: `{tileUrl}/{width}/{height}/{zoom}/{x}/{y}.png`

### 3.5 Romanian Cities Database

| Detail | Value |
|--------|-------|
| **Source** | `https://raw.githubusercontent.com/romania/localitati/master/orase.csv` |
| **License** | WTFPL (public domain equivalent) |
| **Entries** | 13,756 Romanian localities with WGS84 coordinates |
| **Usage** | Bundled as `app/src/main/assets/orase.json` for offline city search |

### 3.6 WMO Weather Codes

The app uses standard WMO weather interpretation codes (factual data, not copyrightable) mapped to Romanian and English descriptions + icon identifiers. See `WeatherCode.kt` for the complete mapping.

---

## 4. Build System

### Versions (Locked & Verified)

| Component | Version | Source | Status |
|-----------|---------|--------|--------|
| **AGP** | 8.9.1 | Google Maven | ✅ Confirmed |
| **Gradle** | 8.11.1 | services.gradle.org | ✅ Confirmed |
| **Kotlin** | 2.1.0 | GitHub Releases | ✅ Confirmed |
| **KSP** | 2.1.0-1.0.29 | Maven Central | ✅ Confirmed |
| **Compose BOM** | 2026.05.01 | Google Maven | ✅ Confirmed (Material3 1.4.0) |
| **Android SDK** | 35 (compile), 26 (min), 35 (target) | Android SDK Manager | ✅ Installed |
| **Build Tools** | 35.0.0 | Google Maven | ✅ Confirmed |
| **Java** | 17.0.19 | OpenJDK | ✅ Installed |

### Key Dependencies

```kotlin
// Compose BOM — manages all Compose versions
implementation(platform("androidx.compose:compose-bom:2026.05.01"))

// Core AndroidX
androidx.core:core-ktx:1.15.0
androidx.lifecycle:lifecycle-runtime-ktx:2.8.7
androidx.activity:activity-compose:1.9.3

// Compose UI
androidx.compose.ui:ui
androidx.compose.ui:ui-graphics
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended
androidx.compose.animation:animation

// Navigation
androidx.navigation:navigation-compose:2.8.5

// Networking
com.squareup.retrofit2:retrofit:2.11.0
com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0
com.squareup.okhttp3:okhttp:4.12.0
com.squareup.okhttp3:logging-interceptor:4.12.0

// Serialization
org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3

// Room (via KSP, NOT kapt/annotationProcessor)
androidx.room:room-runtime:2.8.4
androidx.room:room-ktx:2.8.4
ksp("androidx.room:room-compiler:2.8.4")

// DataStore
androidx.datastore:datastore-preferences:1.1.1

// WorkManager
androidx.work:work-runtime-ktx:2.11.1

// MapLibre (with Compose wrapper)
org.maplibre.gl:android-sdk:13.2.0
org.maplibre.gl:maplibre-compose:0.13.0

// Lottie Animations
com.airbnb.android:lottie-compose:6.6.2

// Image Loading
io.coil-kt:coil-compose:2.7.0

// Location
com.google.android.gms:play-services-location:21.3.0

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
testImplementation("app.cash.turbine:turbine:1.2.0")
testImplementation("io.mockk:mockk:1.13.13")
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
androidTestImplementation("androidx.test.ext:junit:1.2.1")
androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
debugImplementation("androidx.compose.ui:ui-tooling")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

### Gradle Properties

```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

### Build Configuration

**Root `build.gradle.kts`:**
```kotlin
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
}
```

**App `build.gradle.kts` — Build Types:**
- `debug`: debuggable, `.debug` suffix
- `release`: minified, shrunk resources, ProGuard optimized

### Critical Build Fixes Applied

| Issue | Fix |
|-------|-----|
| AGP 8.7.2 + Gradle 8.11.1 incompatibility | Upgraded AGP to 8.9.1 |
| Room `annotationProcessor` | Changed to `ksp()` (kapt deprecated) |
| Compose BOM pinned to 2025.01.01 | Updated to 2026.05.01 |
| MapLibre 11.5.2 (no Compose wrapper) | Updated to 13.2.0 + maplibre-compose 0.13.0 |
| WorkManager 2.10.0 | Updated to 2.11.1 (requires `getForegroundInfo()`) |
| Room 2.6.1 | Updated to 2.8.4 |
| Missing test deps | Added MockK, Turbine, MockWebServer, Kover |
| `toMediaType()` extension | Fixed OkHttp import path |

---

## 5. Project Structure

```
VremeaRomaniei/
├── app/
│   ├── build.gradle.kts              # App-level build config
│   ├── proguard-rules.pro             # ProGuard rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml    # App manifest
│       │   ├── assets/
│       │   │   └── orase.json         # 13,756 Romanian cities (WGS84)
│       │   ├── java/com/vremea/romaniei/
│       │   │   ├── VremeaApp.kt                  # Application class (WorkManager init)
│       │   │   ├── MainActivity.kt                # Single activity entry point
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── AppDatabase.kt         # Room database (entities, DAOs)
│       │   │   │   │   ├── PreferencesDataStore.kt # Settings persistence
│       │   │   │   │   ├── WeatherUpdateWorker.kt # WorkManager periodic worker
│       │   │   │   │   ├── AlertWorker.kt         # Background alert check worker
│       │   │   │   │   ├── BootReceiver.kt        # Re-schedule on boot
│       │   │   │   │   ├── AlertNotificationManager.kt # Notification channels & display
│       │   │   │   │   └── dao/                   # Room DAO interfaces
│       │   │   │   │       ├── WeatherDao.kt
│       │   │   │   │       ├── AlertDao.kt
│       │   │   │   │       └── LocationDao.kt
│       │   │   │   ├── remote/
│       │   │   │   │   ├── NetworkClient.kt       # Retrofit + OkHttp setup
│       │   │   │   │   ├── OpenMeteoApi.kt        # Open-Meteo endpoints
│       │   │   │   │   ├── AnmApi.kt              # ANM JSON API (wp-json)
│       │   │   │   │   ├── MeteoAlarmApi.kt       # MeteoAlarm Atom feed
│       │   │   │   │   ├── RainViewerApi.kt       # RainViewer radar manifest
│       │   │   │   │   └── dto/
│       │   │   │   │       ├── OpenMeteoDto.kt    # Open-Meteo response types
│       │   │   │   │       ├── AnmDto.kt          # ANM GeoJSON types
│       │   │   │   │       └── RainViewerDto.kt   # RainViewer manifest types
│       │   │   │   └── repository/
│       │   │   │       ├── WeatherRepository.kt   # Weather data + cache
│       │   │   │       ├── LocationRepository.kt  # City search (bundled + API)
│       │   │   │       └── AlertRepository.kt     # ANM + MeteoAlarm aggregation
│       │   │   ├── domain/
│       │   │   │   └── model/
│       │   │   │       ├── WeatherData.kt         # Domain weather models
│       │   │   │       ├── LocationData.kt        # Location model
│       │   │   │       ├── WeatherCode.kt         # WMO code → description/icon
│       │   │   │       └── WeatherExtensions.kt   # Formatting utilities
│       │   │   ├── ui/
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Color.kt               # App color palette
│       │   │   │   │   ├── Theme.kt               # Material3 theme
│       │   │   │   │   └── Type.kt                # Typography
│       │   │   │   ├── navigation/
│       │   │   │   │   └── AppNavigation.kt       # NavHost + bottom nav
│       │   │   │   ├── components/
│       │   │   │   │   ├── CurrentWeatherCard.kt   # Main weather display
│       │   │   │   │   ├── HourlyForecastRow.kt   # 24h scrollable row
│       │   │   │   │   ├── DailyForecastCard.kt   # Single day forecast
│       │   │   │   │   ├── WeatherDetailRow.kt    # Detail grid cards
│       │   │   │   │   └── AlertBanner.kt         # Alert severity card
│       │   │   │   └── screens/
│       │   │   │       ├── home/
│       │   │   │       │   ├── HomeScreen.kt      # Main weather screen
│       │   │   │       │   └── HomeViewModel.kt   # Home state management
│       │   │   │       ├── forecast/
│       │   │   │       │   ├── ForecastScreen.kt  # 16-day forecast list
│       │   │   │       │   └── ForecastViewModel.kt
│       │   │   │       ├── map/
│       │   │   │       │   ├── MapScreen.kt       # MapLibre + radar
│       │   │   │       │   └── MapViewModel.kt    # Map state management
│       │   │   │       ├── alerts/
│       │   │   │       │   ├── AlertsScreen.kt    # Alert list display
│       │   │   │       │   └── AlertsViewModel.kt # Alert loading + state
│       │   │   │       └── settings/
│       │   │   │           ├── SettingsScreen.kt  # Settings UI
│       │   │   │           └── SettingsViewModel.kt # Settings state (DataStore-backed)
│       │   │   ├── notification/
│       │   │   │   └── AlertNotificationManager.kt # Channel setup + display
│       │   │   └── util/
│       │   │       ├── DateUtils.kt               # Date formatting helpers
│       │   │       └── CoordinateUtils.kt          # Stereo 70 → WGS84 (future use)
│       │   ├── res/
│       │   │   ├── drawable/
│       │   │   │   ├── ic_launcher_foreground.xml # Adaptive icon foreground
│       │   │   │   └── ic_launcher_background.xml # Adaptive icon background
│       │   │   ├── mipmap-anydpi-v26/
│       │   │   │   ├── ic_launcher.xml            # Adaptive icon
│       │   │   │   └── ic_launcher_round.xml      # Adaptive round icon
│       │   │   ├── mipmap-hdpi/                   # Legacy launcher icons
│       │   │   ├── mipmap-mdpi/
│       │   │   ├── mipmap-xhdpi/
│       │   │   ├── mipmap-xxhdpi/
│       │   │   ├── mipmap-xxxhdpi/
│       │   │   ├── values/
│       │   │   │   ├── strings.xml                # English strings (default)
│       │   │   │   └── themes.xml                 # XML theme
│       │   │   └── values-ro/
│       │   │       └── strings.xml                # Romanian strings
│       │   └── test/                              # Unit tests
│       │       └── java/com/vremea/romaniei/
│       │           ├── data/
│       │           │   ├── WeatherRepositoryTest.kt
│       │           │   ├── LocationRepositoryTest.kt
│       │           │   └── AlertRepositoryTest.kt
│       │           ├── domain/
│       │           │   ├── WeatherCodeTest.kt
│       │           │   └── WeatherExtensionsTest.kt
│       │           └── ui/
│       │               ├── HomeViewModelTest.kt
│       │               ├── ForecastViewModelTest.kt
│       │               └── SettingsViewModelTest.kt
│       ├── build.gradle.kts                  # Root build config
│       ├── gradle/
│       │   └── wrapper/
│       │       ├── gradle-wrapper.jar        # Gradle wrapper binary
│       │       └── gradle-wrapper.properties # Gradle 8.11.1
│       ├── gradle.properties                 # JVM args, caching settings
│       ├── gradlew                           # Unix Gradle wrapper script
│       ├── gradlew.bat                       # Windows Gradle wrapper script
│       ├── local.properties                  # SDK path (user-specific)
│       ├── scripts/
│       │   └── build.sh                      # Convenience build script
│       ├── settings.gradle.kts               # Project settings + repos
│       └── .github/
│           └── workflows/
│               ├── build.yml                 # CI: build + test on push/PR
│               └── release.yml               # CD: sign + release on tag
```

---

## 6. Data Layer

### 6.1 Room Database (AppDatabase.kt)

```kotlin
@Database(
    entities = [WeatherEntity::class, AlertEntity::class, LocationEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun alertDao(): AlertDao
    abstract fun locationDao(): LocationDao
}
```

**Entities:**

| Entity | Key Fields | Purpose |
|--------|------------|---------|
| `WeatherEntity` | `id` (lat+lon composite), `jsonData`, `lastUpdated`, `expiresAt` | Cached weather response |
| `AlertEntity` | `id` (alert GUID), `title`, `severity`, `description`, `startTime`, `endTime`, `source`, `locationNames` | Cached weather alerts |
| `LocationEntity` | `id` (city code), `name`, `county`, `latitude`, `longitude`, `population` | Searched/browsed locations |

**DAOs:**

| DAO | Methods | Cache Strategy |
|-----|---------|----------------|
| `WeatherDao` | `getWeather(lat, lon)`, `insert(weather)`, `deleteExpired(now)` | TTL: 30 min weather, 2 hours forecast |
| `AlertDao` | `getActiveAlerts(now)`, `insertAll(alerts)`, `deleteAll()` | Replace on each sync |
| `LocationDao` | `searchByName(query)`, `getAll()`, `insertAll(locations)` | Static (bundled) |

### 6.2 Network Layer

**NetworkClient.kt** — Singleton Retrofit instance factory:
```kotlin
object NetworkClient {
    // Open-Meteo Retrofit (api.open-meteo.com)
    val openMeteoApi: OpenMeteoApi
    
    // ANM Retrofit (www.meteoromania.ro/wp-json/meteoapi/v2/)
    val anmApi: AnmApi
    
    // MeteoAlarm (Atom feed)
    val meteoAlarmApi: MeteoAlarmApi
    
    // RainViewer (api.rainviewer.com)
    val rainViewerApi: RainViewerApi
}
```

**API Interfaces:**

| Interface | Endpoint | Method | Returns |
|-----------|----------|--------|---------|
| `OpenMeteoApi` | `v1/forecast` | GET | `OpenMeteoResponse` (JSON) |
| `OpenMeteoApi` | `v1/air-quality` | GET | `OpenMeteoResponse` (JSON) |
| `OpenMeteoApi` | `v1/geocoding` | GET | `OpenMeteoResponse` (JSON) |
| `AnmApi` | `wp-json/meteoapi/v2/avertizari-generale` | GET | `AnmWarningsResponse` (JSON) |
| `AnmApi` | `wp-json/meteoapi/v2/avertizari-nowcasting` | GET | `AnmWarningsResponse` (JSON) |
| `AnmApi` | `wp-json/meteoapi/v2/prognoza-orase` | GET | `AnmCityForecastResponse` (JSON) |
| `MeteoAlarmApi` | (Atom feed URL) | GET | Parsed `AlertData[]` (XML) |
| `RainViewerApi` | `public/weather-maps.json` | GET | `RainViewerManifest` (JSON) |

### 6.3 Repository Layer

**WeatherRepository** — Primary data orchestration:
```kotlin
class WeatherRepository(
    private val api: OpenMeteoApi,
    private val weatherDao: WeatherDao
) {
    // Tries cache first, then network
    // Cache TTL: 30 min for current, 2 hours for daily
    suspend fun getForecast(lat: Double, lon: Double): Result<WeatherData>
    suspend fun searchLocations(query: String): Result<List<LocationData>>
    suspend fun getAirQuality(lat: Double, lon: Double): Result<AirQuality>
}
```

**LocationRepository** — City search:
```kotlin
class LocationRepository(
    private val appContext: Context,
    private val locationDao: LocationDao
) {
    // Loads bundled orase.json into Room on first launch
    // Searches Room for instant results
    suspend fun searchCities(query: String): List<LocationData>
    suspend fun getNearbyCities(lat: Double, lon: Double): List<LocationData>
}
```

**AlertRepository** — Alert aggregation:
```kotlin
class AlertRepository(
    private val anmApi: AnmApi,
    private val meteoAlarmApi: MeteoAlarmApi,
    private val alertDao: AlertDao
) {
    // Aggregates ANM + MeteoAlarm into unified AlertData list
    // Caches in Room, replaces on each successful sync
    suspend fun getActiveAlerts(): List<AlertData>
    suspend fun refreshAlerts(): Result<List<AlertData>>
}
```

---

## 7. Domain Layer

### Data Models

```kotlin
data class WeatherData(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather?,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val alerts: List<AlertData>,
    val airQuality: AirQuality?,
    val lastUpdated: Long
)

data class CurrentWeather(
    val temperature: Double,      // °C
    val feelsLike: Double,        // °C
    val humidity: Int,            // %
    val pressure: Double,         // hPa
    val windSpeed: Double,        // km/h
    val windDirection: Int,       // degrees (0-360)
    val windGusts: Double,        // km/h
    val cloudCover: Int,          // %
    val visibility: Double,       // meters
    val uvIndex: Double,          // 0-11+
    val weatherCode: Int,         // WMO code (0-99)
    val weatherDescription: String,
    val weatherIcon: String,
    val isDay: Boolean
)

data class AlertData(
    val id: String,
    val title: String,
    val description: String,
    val severity: AlertSeverity,  // MINOR, MODERATE, SEVERE, EXTREME
    val eventType: String,
    val startTime: Long,
    val endTime: Long,
    val source: String,          // "ANM" or "MeteoAlarm EU"
    val locationNames: List<String>,
    val colorHex: String
)
```

### WMO Weather Code Mapping

The `WeatherCode` object maps WMO codes (0-99) to:
- **English descriptions** (e.g., "Clear Sky", "Moderate Rain")
- **Romanian descriptions** (e.g., "Cer Senin", "Ploaie Moderată")
- **Icon identifiers** (e.g., "sunny", "rainy", "thunderstorm") for UI rendering

### Formatting Utilities (`WeatherExtensions.kt`)

| Function | Input | Output |
|----------|-------|--------|
| `Int.toWindDirection()` | 0-360 degrees | "N", "NE", "E", ... |
| `Int.toWindDirectionRo()` | 0-360 degrees | "N", "NE", "E", ... (Romanian abbreviations) |
| `Double.toPressure()` | hPa | "1013.2" |
| `Int.toVisibility()` | meters | "10.0km" or "800m" |
| `Double.toKmh()` | km/h | "15 km/h" |
| `Int.toUvDescription()` | 0-11+ | "Low", "Moderate", "High", "Very High", "Extreme" |
| `Int.toUvDescriptionRo()` | 0-11+ | "Scăzut", "Moderat", "Ridicat", "Foarte Ridicat", "Extrem" |

---

## 8. UI Layer

### 8.1 Theme (`ui/theme/`)

**Color Palette (`Color.kt`):**
- **Primary**: Weather blue (`#1A73E8`)
- **Secondary**: Teal accent
- **Tertiary**: Warm amber
- **Alert colors**: `AlertYellow`, `AlertOrange`, `AlertRed` (for severity levels)
- **Day/Night**: Full light and dark color schemes

**Typography (`Type.kt`):**
- Clean, readable Material3 defaults
- Custom `displayLarge` for temperature display (bold)

**Theme (`Theme.kt`):**
- `VremeaRomanieiTheme` composable wrapper
- Supports `system` / `light` / `dark` modes via `SettingsViewModel`
- Dynamic color scheme based on current theme mode
- Uses Material3 `lightColorScheme()` / `darkColorScheme()`

### 8.2 Navigation (`AppNavigation.kt`)

**Bottom Navigation (5 tabs):**

| Tab | Route | Icon | Screen |
|-----|-------|------|--------|
| Acasă | `home` | `Icons.Filled.Cloud` | `HomeScreen` |
| Prognoză | `forecast` | `Icons.Filled.DateRange` | `ForecastScreen` |
| Hartă | `map` | `Icons.Filled.Map` | `MapScreen` |
| Alerte | `alerts` | `Icons.Filled.Notifications` | `AlertsScreen` |
| Setări | `settings` | `Icons.Filled.Settings` | `SettingsScreen` |

Features:
- `NavigationBar` with `NavigationBarItem` for each tab
- `popUpTo` with `saveState`/`restoreState` for proper back stack
- `launchSingleTop` to prevent duplicate destinations

### 8.3 Screens

#### HomeScreen
- **Purpose**: Main weather display — current conditions + hourly forecast + details
- **State**: `HomeViewModel` with `WeatherUiState` (Loading/Success/Error)
- **Features**: Swipe-to-refresh, city search (TODO), current location (TODO)
- **Components**: `CurrentWeatherCard`, `HourlyForecastRow`, `WeatherDetailRow`

#### ForecastScreen
- **Purpose**: 16-day daily forecast in scrollable list
- **State**: `ForecastViewModel` with `ForecastUiState`
- **Components**: `DailyForecastCard` (day name, icon, description, temp range, precip%)

#### MapScreen
- **Purpose**: Interactive weather map with layer switching
- **State**: `MapViewModel` with `MapUiState` (center, zoom, activeLayer, fullscreen)
- **Map engine**: MapLibre GL 13.2.0 via `AndroidView`
- **Layers**: Temperature, Precipitation, Wind, Radar (RainViewer tiles)
- **Controls**: Layer selector FABs, attribution text overlay
- **⚠️ Known issue**: MapLibre lifecycle not managed via `DisposableEffect` — fixed in Phase 3

#### AlertsScreen
- **Purpose**: Display active weather warnings from ANM + MeteoAlarm EU
- **State**: `AlertsViewModel` with `AlertsUiState`
- **Features**: ANM alert banner at top, MeteoAlarm list below
- **⚠️ Known issue**: Currently hardcoded sample data — wired to live APIs in Phase 3

#### SettingsScreen
- **Purpose**: App configuration
- **Settings**: Language (RO/EN), Temperature unit (°C/°F), Theme (system/light/dark), Notifications on/off, Update interval (1/2/4/6 hours)
- **Persistence**: Reads/writes `PreferencesDataStore`
- **⚠️ Known issue**: Currently in-memory only — wired to DataStore in Phase 3

### 8.4 Reusable Components

| Component | Location | Props | Purpose |
|-----------|----------|-------|---------|
| `CurrentWeatherCard` | `ui/components/` | `weather: WeatherData` | Large temperature card with icon, description, detail chips |
| `HourlyForecastRow` | `ui/components/` | `hourlyData: List<HourlyWeather>` | Horizontal scrolling 24h forecast |
| `DailyForecastCard` | `ui/components/` | `day: DailyWeather` | Single day row with icon, temp range |
| `WeatherDetailRow` | `ui/components/` | `weather: WeatherData` | Grid of detail cards (pressure, wind, gusts, UV) + sunrise/sunset |
| `AlertBanner` | `ui/components/` | `alert: AlertData` | Color-coded alert card by severity |

---

## 9. Notification System

### Architecture

```
BootReceiver          AlertWorker          AlertNotificationManager
    │                     │                       │
    │ on BOOT_COMPLETED   │ doWork()              │ createChannels()
    │ re-schedule workers │ fetch ANM+MeteoAlarm  │ showNotification()
    │                     │ diff with Room cache  │
    │                     │ show if new/updated   │
    │                     │                       │
    └─────────────────────┴───────────────────────┘
                VremeaApp.onCreate()
                schedules PeriodicWorkRequest
```

### Components

**`AlertNotificationManager.kt`:**
- Creates notification channel (`ALERTS_CHANNEL_ID = "vremea_alerts"`)
- Channel name: "Alerte Meteo" / "Weather Alerts" (locale-dependent)
- Importance: `IMPORTANCE_HIGH` (heads-up notification for severe alerts)
- Creates `NotificationCompat.Builder` with severity-based color
- Handles Android 13+ `POST_NOTIFICATIONS` permission

**`AlertWorker.kt`** (extends `CoroutineWorker`):
- Fetches current alerts from `AlertRepository`
- Compares with Room-cached alerts
- Shows notification ONLY for new or upgraded severity alerts
- Implements `getForegroundInfo()` (required for WorkManager 2.10+ on API 31+)
- Runs every 2 hours (configurable via settings)

**`WeatherUpdateWorker.kt`:**
- Periodic background weather data refresh (replaced by AlertWorker for alerts)
- Implements `getForegroundInfo()` override

**`BootReceiver.kt`:**
- Listens for `android.intent.action.BOOT_COMPLETED`
- Re-schedules all WorkManager periodic tasks
- Required because WorkManager jobs do not persist across reboots on all devices

**`VremeaApp.kt`:**
- `Application` class implementing `Configuration.Provider`
- Schedules `PeriodicWorkRequest` for alert checks on app launch
- Sets `WorkManager` logging level

### Notification Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

---

## 10. Map & Radar Features

### MapLibre GL Integration

- **SDK**: `org.maplibre.gl:android-sdk:13.2.0`
- **Compose Wrapper**: `org.maplibre.gl:maplibre-compose:0.13.0`
- **Style URL**: `https://demotiles.maplibre.org/style.json` (default)
- **Rendered via**: `AndroidView` with lifecycle-aware `DisposableEffect`

### Map Layers

| Layer | Description | Data Source |
|-------|-------------|-------------|
| Temperature | Color overlay | Open-Meteo (via MapLibre GeoJSON source) |
| Precipitation | Rain intensity | Open-Meteo hourly data |
| Wind | Wind barbs/arrows | Open-Meteo wind data |
| Radar | Animated rain radar | RainViewer tile API |

### RainViewer Radar Animation

1. Fetch manifest from `https://api.rainviewer.com/public/weather-maps.json`
2. Parse `past[]` array for historical frames (timestamps)
3. For each frame, construct tile URL:
   `https://tilecache.rainviewer.com/v2/radar/{timestamp}/256/{zoom}/{x}/{y}.png`
4. Add as raster tile source to MapLibre
5. Animate through frames using frame timer (configurable interval)

### Map Controls

- **Layer switcher**: FAB column (bottom-right) with °C, 💧, 💨, 📡 buttons
- **Center**: Romania centroid (45.9432°N, 24.9668°E), zoom 6.5
- **Current location**: FusedLocationProviderClient (button in top bar) — uses `ACCESS_FINE_LOCATION`
- **Attribution**: OpenStreetMap contributors + MapLibre (bottom center overlay)

---

## 11. Offline Mode

### Strategy

The app uses an **offline-first** caching strategy:

1. **On network available**: Fetch fresh data from APIs, store in Room, display
2. **On network unavailable**: Load from Room cache, display stale data with "offline" indicator
3. **On cache miss + no network**: Show error state with "retry" button
4. **On background sync**: WorkManager refreshes cache periodically

### Cache TTLs

| Data Type | TTL | Invalidation |
|-----------|-----|-------------|
| Current weather | 30 minutes | `WHERE lastUpdated + 1800000 < ?` |
| Daily forecast | 2 hours | `WHERE lastUpdated + 7200000 < ?` |
| Weather alerts | On each sync | Delete all, insert fresh |
| City list | Static (never expires) | Bundled in APK |
| User settings | N/A (DataStore) | Instant |

### Entity Schema

```kotlin
@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val id: String,    // "lat,lon" composite key
    val jsonData: String,          // Serialized JSON response
    val lastUpdated: Long,
    val expiresAt: Long
)

@Entity(tableName = "alert_cache")
data class AlertEntity(
    @PrimaryKey val id: String,    // Alert GUID from source
    val jsonData: String,
    val lastUpdated: Long
)

@Entity(tableName = "location_cache")
data class LocationEntity(
    @PrimaryKey val id: String,    // City code or "lat,lon"
    val name: String,
    val county: String?,
    val latitude: Double,
    val longitude: Double,
    val population: Int?
)
```

---

## 12. Localization (i18n)

### Supported Locales

| Locale | File | Status |
|--------|------|--------|
| Romanian (RO) | `res/values-ro/strings.xml` | ✅ Complete (55 strings) |
| English (EN) | `res/values/strings.xml` (default) | ✅ Complete (55 strings) |

### String Coverage

The app provides localized strings for:

- **Navigation**: Tab labels (Acasă/Home, Prognoză/Forecast, Hartă/Map, Alerte/Alerte, Setări/Settings)
- **Weather**: Current weather, hourly forecast, daily forecast, all detail fields
- **UI Actions**: Loading, error, retry, refresh, search city
- **Settings**: Language, temperature unit, theme, notifications, update interval
- **Map**: Layer names (temperature, precipitation, wind, radar), attribution
- **Weather**: Feels like, humidity, wind, cloud cover, visibility, pressure, UV, gusts, sunrise, sunset, precipitation

### Dynamic Localization

- **Locale auto-detection**: On first launch, reads device locale. If Romanian, sets RO; otherwise EN
- **Manual override**: Settings screen allows switching at any time
- **Weather descriptions**: `WeatherCode` object supports both EN and RO via `getDescription(code, ro)`
- **Wind direction**: Separate functions for EN (`toWindDirection()`) and RO (`toWindDirectionRo()`)
- **UV descriptions**: Separate functions for EN (`toUvDescription()`) and RO (`toUvDescriptionRo()`)

---

## 13. Testing Strategy

### Test Categories

| Category | Framework | Location | Coverage Target |
|----------|-----------|----------|-----------------|
| **Unit Tests** | JUnit 5 + MockK | `app/src/test/` | ViewModels, Repositories, domain logic |
| **Integration** | MockWebServer + Room | `app/src/test/` | API clients, DAOs |
| **UI Tests** | Compose UI Test | `app/src/androidTest/` | Screen rendering, navigation |
| **Coverage** | Kover | Build plugin | Minimum 70% line coverage |

### Test Files

```
app/src/test/java/com/vremea/romaniei/
├── data/
│   ├── WeatherRepositoryTest.kt     # Mock API + Room DAO → verify caching logic
│   ├── LocationRepositoryTest.kt    # Mock orase.json → verify search
│   ├── AlertRepositoryTest.kt       # Mock ANM + MeteoAlarm → verify merge
│   └── remote/
│       ├── OpenMeteoApiTest.kt      # MockWebServer → verify serialization
│       ├── AnmApiTest.kt            # MockWebServer → verify JSON parsing
│       └── RainViewerApiTest.kt     # MockWebServer → verify manifest parsing
├── domain/
│   ├── WeatherCodeTest.kt           # All WMO codes → verify descriptions and icons
│   └── WeatherExtensionsTest.kt     # Formatting functions → verify output
├── ui/
│   ├── HomeViewModelTest.kt         # Mock repository → verify state transitions
│   ├── ForecastViewModelTest.kt     # Mock repository → verify forecast loading
│   └── AlertsViewModelTest.kt       # Mock AlertRepository → verify alert states
└── data/local/
    ├── WeatherDaoTest.kt            # Room in-memory DB → verify CRUD + expiry
    └── PreferencesDataStoreTest.kt  # DataStore test → verify read/write
```

### Key Test Patterns

**Repository tests** (use MockK for API + Room in-memory DB):
```kotlin
@Test
fun `getForecast returns cached data when not expired`() = runTest {
    val dao = mockWeatherDao(cachedData = validEntity)
    val api = mockOpenMeteoApi() // never called
    val repo = WeatherRepository(api, dao)
    
    val result = repo.getForecast(44.43, 26.10)
    assertTrue(result.isSuccess)
    verify(api, never()).getForecast(any(), any())
}
```

**ViewModel tests** (use Turbine for StateFlow testing):
```kotlin
@Test
fun `loading state transitions to success`() = runTest {
    val repo = mockWeatherRepository(successData = sampleWeather)
    val vm = HomeViewModel(repo)
    
    vm.weatherState.test {
        assertEquals(WeatherUiState.Loading, awaitItem())
        vm.loadWeather(44.43, 26.10)
        assertTrue(awaitItem() is WeatherUiState.Success)
    }
}
```

---

## 14. CI/CD Pipeline

### GitHub Actions Workflows

#### `build.yml` — Continuous Integration

```yaml
name: Build & Test
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v3
      - run: ./gradlew assembleDebug
      - run: ./gradlew test
      - run: ./gradlew lint
      - uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
```

#### `release.yml` — Continuous Delivery

```yaml
name: Release
on:
  push:
    tags: ['v*']

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - run: ./gradlew assembleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      - uses: actions/create-release@v1
        with:
          tag_name: ${{ github.ref_name }}
          release_name: VremeaRomâniei ${{ github.ref_name }}
          body: 'See CHANGELOG for details.'
      - uses: actions/upload-release-asset@v1
        with:
          upload_url: ${{ steps.create_release.outputs.upload_url }}
          asset_path: app/build/outputs/apk/release/*.apk
```

### Signing Configuration

```kotlin
// app/build.gradle.kts
signingConfigs {
    create("release") {
        storeFile = file("vremea-romaniei.keystore")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        keyAlias = System.getenv("KEY_ALIAS") ?: "nedaktov"
        keyPassword = System.getenv("KEY_PASSWORD") ?: ""
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ...
    }
}
```

### Secrets Required

| Secret | Usage |
|--------|-------|
| `KEYSTORE_PASSWORD` | Keystore master password |
| `KEY_ALIAS` | Signing key alias (`nedaktov`) |
| `KEY_PASSWORD` | Signing key password |

---

## 15. Distribution

### Channels

| Channel | Method | Cost | Coverage |
|---------|--------|------|----------|
| **GitHub Releases** | Tag push triggers release workflow | Free | Anyone with GitHub link |
| **IzzyOnDroid** | Submit APK + metadata via PR | Free | F-Droid client users |

### IzzyOnDroid Setup

1. Create `metadata/` directory with:
   - `metadata/com.vremea.romaniei.yml` — app metadata
   - `metadata/com.vremea.romaniei/en-US/description.txt` — full description
   - `metadata/com.vremea.romaniei/en-US/short_description.txt` — tagline
   - `metadata/com.vremea.romaniei/ro/description.txt` — Romanian description
   - Screenshots in `metadata/com.vremea.romaniei/en-US/graphics/`

2. Submit to IzzyOnDroid via GitHub PR to `IzzyOnDroid/repo`

3. Future updates: Automated via GitHub tag push + IzzyOnDroid's weekly scan

### Required Screenshots

| Screen | Content |
|--------|---------|
| Home screen | Current weather with temperature, icon, details |
| Forecast | 16-day scrollable list |
| Map | MapLibre with radar overlay |
| Alerts | ANM + MeteoAlarm warnings |
| Settings | Language, theme, notification toggles |

---

## 16. Development Setup

### Prerequisites

```bash
# System requirements (Linux Mint 22.3 tested)
- OpenJDK 17+ (Java 17.0.19 confirmed)
- Android SDK (platforms 33+)
- Build tools 34.0.0+
- Git 2.43+
- 8GB+ RAM recommended
- 10GB+ free disk

# Android SDK setup (if not installed)
export ANDROID_HOME="$HOME/Android/Sdk"
sdkmanager "platforms;android-35" "build-tools;35.0.0" "ndk;27.0.12077973"
```

### Quick Start

```bash
# Clone
git clone https://github.com/nedaktov-ops/vremea-romaniei.git
cd vremea-romaniei

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./scripts/build.sh install

# Run tests
./scripts/build.sh test
```

### Environment Variables

```bash
# Required for release builds only
export ANDROID_HOME="$HOME/Android/Sdk"
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"

# Required for signed release
export KEYSTORE_PASSWORD="..."
export KEY_ALIAS="nedaktov"
export KEY_PASSWORD="..."
```

### Device Testing

- Physical device recommended (emulator not setup for API 35)
- Connect via USB with USB debugging enabled
- Use `./gradlew installDebug` for direct installation
- ADB version 1.0.41 confirmed working

---

## 17. Build Scripts

### `scripts/build.sh`

Convenience wrapper around Gradle tasks:

```bash
./scripts/build.sh clean    # Clean build artifacts
./scripts/build.sh build    # Build debug APK
./scripts/build.sh release  # Build release APK (requires signing config)
./scripts/build.sh install  # Install debug APK on connected device
./scripts/build.sh test     # Run unit tests
./scripts/build.sh lint     # Run Android lint checks
```

### Manual Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (signed)
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run lint
./gradlew lint

# Clean
./gradlew clean

# Run specific test
./gradlew test --tests "com.vremea.romaniei.data.WeatherRepositoryTest"
```

### First Build Notes

- First build takes 5-15 minutes (dependency downloads, configuration cache)
- Use `--no-configuration-cache` for first build if needed
- Subsequent builds are faster (cached dependencies)
- Build output: `app/build/outputs/apk/debug/app-debug.apk`

---

## 18. Troubleshooting

### Common Issues

| Symptom | Likely Cause | Fix |
|---------|--------------|-----|
| `annotationProcessor` vs `ksp` error | Room using annotationProcessor | Change to `ksp("androidx.room:room-compiler:2.8.4")` |
| `getForegroundInfo()` crash on API 31+ | Worker missing override | Add `getForegroundInfo()` to `CoroutineWorker` subclass |
| ANM GeoJSON coordinates wrong | Stereo 70 vs WGS84 | Use Open-Meteo geocoding instead of ANM station coords |
| MeteoAlarm 404 | Using old JSON feed URL | Switch to Atom XML feed endpoint |
| Build timeout (300s) | First-run Gradle | Use `--no-configuration-cache` or increase terminal timeout |
| MapLibre lifecycle crash | Missing `DisposableEffect` | Add lifecycle-aware disposal in MapScreen |
| Room DB schema mismatch | Schema changes without migration | Bump version or use `fallbackToDestructiveMigration()` |
| `toMediaType()` | Wrong OkHttp import | Use `"application/json".toMediaType()` from `okhttp3.MediaType.Companion` |

### Build Fix Reference

| File | Issue | Fix Applied |
|------|-------|-------------|
| `build.gradle.kts` (root) | AGP 8.7.2 | → 8.9.1 |
| `app/build.gradle.kts` | Compose BOM 2025.01.01 | → 2026.05.01 |
| `app/build.gradle.kts` | Room annotationProcessor | → ksp() |
| `app/build.gradle.kts` | MapLibre 11.5.2 | → 13.2.0 + maplibre-compose 0.13.0 |
| `app/build.gradle.kts` | WorkManager 2.10.0 | → 2.11.1 |
| `app/build.gradle.kts` | Room 2.6.1 | → 2.8.4 |
| `app/build.gradle.kts` | Missing test deps | Added MockK, Turbine, MockWebServer, Kover |
| `gradle-wrapper.properties` | N/A (already 8.11.1) | — |
| `WeatherUpdateWorker.kt` | Missing getForegroundInfo() | Added override |
| `NetworkClient.kt` | Broken toMediaType() | Fixed import |
| `MeteoRomaniaApi.kt` | Targets RSS/XML | Rewrote to target ANM JSON API |
| `OpenMeteoDto.kt` | Has MeteoRomaniaRssResponse/MeteoAlarmResponse | Removed dead types, added ANM/RainViewer DTOs |
| `AppDatabase.kt` | Does not exist | Created with Room entities/DAOs |
| `SettingsScreen.kt` | In-memory state | Wired to PreferencesDataStore |
| `AlertsScreen.kt` | Hardcoded sample data | Connected to live APIs |
| `MapScreen.kt` | Missing lifecycle management | Added DisposableEffect |

---

## 19. License & Attribution

### App License: GPL-3.0

```
VremeaRomâniei — Android Weather App for Romania
Copyright (C) 2026  NedAktov

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
```

GPL-3.0 chosen because it is compatible with **IzzyOnDroid** (F-Droid repository) and ensures source code remains available.

### Third-Party Licenses

| Component | License | Usage |
|-----------|---------|-------|
| **Open-Meteo API** | CC BY 4.0 | Weather data (attribution: link to open-meteo.com) |
| **MeteoAlarm EU** | CC BY 4.0 equivalent | Cross-border weather alerts |
| **ANM Data** | Public sector information | Romanian weather warnings |
| **romania/localitati** | WTFPL | City coordinates database (public domain) |
| **Meteocons** | MIT | Weather icons (basmilius/meteocons) |
| **RainViewer** | Free for non-commercial | Radar overlay tiles |
| **Material Design Icons** | Apache 2.0 | UI icons |
| **MapLibre GL** | BSD | Map rendering engine |
| **OpenStreetMap** | ODbL | Map data (attribution required) |
| **Android Open Source Project** | Apache 2.0 | Android framework |

### WMO Weather Codes

The World Meteorological Organization's weather interpretation codes are factual data and are not subject to copyright restrictions.

### Attribution Text (Displayed in App)

> "Date meteorologice furnizate de Open-Meteo.com (CC BY 4.0) și Administrația Națională de Meteorologie. Hărți © OpenStreetMap contributors (ODbL). Radar © RainViewer."

---

## 20. Key Decisions & Rationale

### Why Kotlin + Compose over Flutter/RN?

| Factor | Kotlin/Compose | Flutter | React Native |
|--------|---------------|---------|--------------|
| Performance | Native (no bridge) | Skia engine | JS bridge overhead |
| MapLibre support | First-class SDK | Third-party | Third-party |
| Room integration | Native | Hive/SQLite | AsyncStorage |
| WorkManager | Native | WorkManager plugin | Background fetch |
| APK Size | ~8MB baseline | ~15MB baseline | ~12MB baseline |

### Why Open-Meteo over WeatherAPI / OpenWeather?

| Factor | Open-Meteo | WeatherAPI (free) | OpenWeather (free) |
|--------|-----------|-------------------|-------------------|
| Cost | Free (no key) | Free (key, 1M/mo) | Free (key, 60/min) |
| Rate limit | 10K/day | 1M/month | 60/min |
| License | CC BY 4.0 | Proprietary | Proprietary |
| Data quality | ECMWF-based | Various | Own model |
| Romanian cities | Good coverage | Limited | Limited |

### Why Manual DI over Hilt?

| Factor | Manual DI | Hilt |
|--------|-----------|------|
| Build time | Faster | +~5s per build |
| APK size | Smaller | +~100KB |
| Learning curve | Minimal | Moderate |
| Boilerplate | More | Less |
| Single-dev project | ✅ Ideal | ✅ Fine |

### Why Room over DataStore for weather cache?

- **Room**: Relational data, complex queries (time-based expiry, lat/lon lookups), batch operations
- **DataStore**: Simple key-value pairs → used only for user settings
- **Trade-off**: Room adds one annotation processor (KSP), but provides proper SQL caching

### Why GPL-3.0 over Apache 2.0?

- **IzzyOnDroid requires** GPL-compatible license
- Ensures the app remains free and open source
- No proprietary dependencies are used

### Why MapLibre over Google Maps?

| Factor | MapLibre | Google Maps |
|--------|----------|-------------|
| Cost | Free (unlimited) | $200/mo credit, then pay |
| API Key | None required | Required |
| Privacy | No Google tracking | Google tracking |
| Customization | Full control | Limited |
| Offline tiles | Supported | Limited |

### Why WorkManager over AlarmManager for notifications?

| Factor | WorkManager | AlarmManager |
|--------|-------------|-------------|
| Doze mode | Works (maintains window) | Killed in Doze |
| Battery optimization | Built-in | Manual |
| Network constraints | Built-in | Manual |
| Persistent across reboot | No (needs BootReceiver) | No (same) |
| Modern API | ✅ Yes | ❌ Legacy |

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| v0.1-scaffold | 2026-06-12 | Project scaffold, build system, 31 Kotlin files, resources |
| v1.0.0 | 2026-06 | Initial release (planned) |

---

> **Built with NedCode3 PNP Orchestration — 121 plugins, 8-phase implementation, 0 API keys.**
> *"The standard isn't 'good enough' — it's 'holy shit, that's done.'"*
