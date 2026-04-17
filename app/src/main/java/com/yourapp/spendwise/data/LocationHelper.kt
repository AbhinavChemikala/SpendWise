package com.yourapp.spendwise.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Lightweight helper that gets the device's current GPS coordinates.
 *
 * Strategy (all best-effort — never throws, returns null on any failure):
 * 1. Check location permission — if denied, return null immediately.
 * 2. Try FusedLocationProviderClient.lastLocation (fast, cached, no battery hit).
 * 3. If lastLocation is null (e.g. device was just booted), request a single
 *    current-location fix with a 5-second timeout.
 */
object LocationHelper {

    private const val TIMEOUT_MS = 5_000L

    fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns a (latitude, longitude) pair or null.
     * Safe to call from any coroutine dispatcher — does not block the caller thread.
     */
    suspend fun getLocation(context: Context): Pair<Double, Double>? {
        if (!hasPermission(context)) return null
        return try {
            withTimeoutOrNull(TIMEOUT_MS) {
                val fused = LocationServices.getFusedLocationProviderClient(context)
                val last = getLastLocation(fused)
                last ?: getCurrentLocation(context, fused)
            }
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("MissingPermission")
    private suspend fun getLastLocation(
        client: com.google.android.gms.location.FusedLocationProviderClient
    ): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
        client.lastLocation
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) cont.resume(loc.latitude to loc.longitude)
                else cont.resume(null)
            }
            .addOnFailureListener { cont.resume(null) }
    }

    @Suppress("MissingPermission")
    private suspend fun getCurrentLocation(
        context: Context,
        client: com.google.android.gms.location.FusedLocationProviderClient
    ): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
        val req = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setMaxUpdateAgeMillis(30_000L)
            .build()
        client.getCurrentLocation(req, null)
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) cont.resume(loc.latitude to loc.longitude)
                else cont.resume(null)
            }
            .addOnFailureListener { cont.resume(null) }
    }
}
