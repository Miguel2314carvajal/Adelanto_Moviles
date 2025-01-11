package com.epn.realidadaumentadaepn.util

import android.location.Location
import kotlin.math.*

object LocationUtils {
    fun calculateBearing(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    ): Float {
        val latitude1 = Math.toRadians(startLat)
        val latitude2 = Math.toRadians(endLat)
        val longDiff = Math.toRadians(endLng - startLng)
        
        val y = sin(longDiff) * cos(latitude2)
        val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff)
        
        return Math.toDegrees(atan2(y, x)).toFloat()
    }

    fun calculateDistance(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }
} 