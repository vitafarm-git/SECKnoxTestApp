package com.knoxprobe.diagnostics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.knoxprobe.domain.LocationSnapshot
import com.knoxprobe.domain.TimeSnapshot
import java.time.OffsetDateTime
import java.time.ZoneId

class TimeLocationDiagnostics(private val context: Context) {
    fun captureTime(): TimeSnapshot {
        val now = OffsetDateTime.now()
        return TimeSnapshot(
            localTimeIso = now.toString(),
            timezoneId = ZoneId.systemDefault().id,
            utcOffsetMinutes = now.offset.totalSeconds / 60,
            elapsedRealtimeMs = SystemClock.elapsedRealtime()
        )
    }

    fun captureLocationSnapshot(): LocationSnapshot {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!coarse && !fine) {
            return LocationSnapshot(available = false, permissionGranted = false, note = "Location permission not granted.")
        }
        val lm = context.getSystemService(LocationManager::class.java)
        val providers = lm.getProviders(true)
        val recent = providers.mapNotNull { provider ->
            try {
                lm.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            }
        }.maxByOrNull { it.time }

        return if (recent != null) {
            LocationSnapshot(
                available = true,
                permissionGranted = true,
                latitude = recent.latitude,
                longitude = recent.longitude,
                accuracyMeters = recent.accuracy,
                provider = recent.provider,
                timestampEpochMs = recent.time,
                note = if (!fine) "Coarse permission only; precision may be reduced." else null
            )
        } else {
            LocationSnapshot(available = false, permissionGranted = true, note = "No recent location fix available from enabled providers.")
        }
    }

    fun timezoneConsistencyNote(locationSnapshot: LocationSnapshot?): String? {
        if (locationSnapshot?.available != true) return null
        return "Heuristic only: timezone/location consistency is not definitive evidence for routing behavior."
    }
}
