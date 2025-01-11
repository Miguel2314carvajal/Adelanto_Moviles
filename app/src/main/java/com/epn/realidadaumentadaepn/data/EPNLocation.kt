package com.epn.realidadaumentadaepn.data

import android.location.Location

data class EPNLocation(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val modelName: String,
    val detectionRange: Float = 20f,
    var distance: Float = 0f
)
