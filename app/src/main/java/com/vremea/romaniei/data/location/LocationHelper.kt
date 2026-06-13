package com.vremea.romaniei.data.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

/**
 * Helper for accessing device location via FusedLocationProviderClient.
 *
 * All permission checks must happen at the UI layer (Composable).
 * This class only requests the last known location — it does not
 * prompt the user for permissions.
 */
class LocationHelper(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Returns the last known location, or null if unavailable.
     * Caller must have [android.Manifest.permission.ACCESS_FINE_LOCATION]
     * or [android.Manifest.permission.ACCESS_COARSE_LOCATION] granted
     * before calling this.
     */
    fun getLastLocation(): Task<Location> {
        return fusedLocationClient.lastLocation
    }

    companion object {
        /** Default fallback: Bucharest city center */
        const val DEFAULT_LAT = 44.4268
        const val DEFAULT_LON = 26.1025

        /** Romania center for map default */
        const val ROMANIA_CENTER_LAT = 45.9432
        const val ROMANIA_CENTER_LON = 24.9668

        fun isLocationPermissionGranted(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
