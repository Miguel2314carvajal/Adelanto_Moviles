package com.epn.realidadaumentadaepn.ui.screens

import android.graphics.ColorSpace.Model
import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.epn.realidadaumentadaepn.R
import com.epn.realidadaumentadaepn.data.EPNLocation
import com.epn.realidadaumentadaepn.data.EPNLocations
import com.epn.realidadaumentadaepn.util.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import com.epn.realidadaumentadaepn.util.LocationUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import android.os.Looper
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.PermissionState
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest.Builder
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import android.location.Location
import kotlin.math.roundToInt
import com.epn.realidadaumentadaepn.util.CompassHelper
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.google.ar.core.Session
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill as DrawScopeFill
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.foundation.background
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import com.google.ar.core.Pose

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ARScreen(navController: NavController, locationName: String) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    val location = EPNLocations.locations.find { it.name == locationName }
    val currentLocation = remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = rememberFusedLocationProviderClient()
    val arrowRotation = remember { mutableStateOf(0f) }
    
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val cameraNode = rememberARCameraNode(engine = engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view = view)
    val planeRenderer = remember {
        mutableStateOf(true)
    }
    val modelInstance = remember {
        mutableListOf<ModelInstance>()
    }
    val trackingFailureReason = remember {
        mutableStateOf<TrackingFailureReason?>(null)
    }
    val frame = remember {
        mutableStateOf<Frame?>(null)
    }
    val compass = remember { CompassHelper(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            planeRenderer = true,
            cameraNode = cameraNode,
            materialLoader = materialLoader,
            onTrackingFailureChanged = {
                trackingFailureReason.value = it
            },
            onSessionUpdated = { session, updatedFrame ->
                frame.value = updatedFrame
                location?.let { targetLocation ->
                    currentLocation.value?.let { current ->
                        val bearing = LocationUtils.calculateBearing(
                            current.latitude, current.longitude,
                            targetLocation.latitude, targetLocation.longitude
                        )
                        arrowRotation.value = bearing
                    }
                }
            },
            sessionConfiguration = { session, config ->
                config.apply {
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    focusMode = Config.FocusMode.AUTO
                }
            },
        )
        
        NavigationOverlay(
            currentLocation = currentLocation.value,
            targetLocation = location,
            arrowRotation = arrowRotation.value
        )
    }
    
    LaunchedEffect(Unit) {
        requestLocationUpdates(fusedLocationClient) { location ->
            currentLocation.value = location
        }
    }

    LaunchedEffect(Unit) {
        compass.start { azimuth ->
            arrowRotation.value = azimuth
        }
    }

    LaunchedEffect(currentLocation.value) {
        currentLocation.value?.let { current ->
            location?.let { target ->
                val distance = LocationUtils.calculateDistance(
                    current.latitude, current.longitude,
                    target.latitude, target.longitude
                )
                
                if (distance < target.detectionRange) {
                    frame.value?.let { frame ->
                        val hits = frame.hitTest(0.5f, 0.5f)
                        hits.firstOrNull { hit ->
                            hit.isValid(depthPoint = true, point = true)
                        }?.let { hit ->
                            // Verifica si ya existe un modelo en esta ubicación
                            val hitPose = hit.hitPose
                            if (childNodes.none { node -> 
                                node.worldPosition.x == hitPose.tx() &&
                                node.worldPosition.y == hitPose.ty() &&
                                node.worldPosition.z == hitPose.tz()
                            }) {
                                hit.createAnchorOrNull()?.let { anchor ->
                                    val modelNode = Utils.createAnchorNode(
                                        engine = engine,
                                        modelLoader = modelLoader,
                                        materialLoader = materialLoader,
                                        modelInstance = modelInstance,
                                        anchor = anchor,
                                        model = "models/${target.modelName}"
                                    ).apply {
                                        scale = Scale(0.5f, 0.5f, 0.5f)
                                    }
                                    childNodes += modelNode
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationOverlay(
    currentLocation: Location?,
    targetLocation: EPNLocation?,
    arrowRotation: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Flecha de navegación
        Image(
            painter = painterResource(id = R.drawable.ar_arrow),
            contentDescription = "Flecha de navegación",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
                .size(60.dp)
                .rotate(arrowRotation)
        )

        // Panel de información
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .padding(16.dp)
        ) {
            targetLocation?.let { target ->
                currentLocation?.let { current ->
                    val distance = LocationUtils.calculateDistance(
                        current.latitude, current.longitude,
                        target.latitude, target.longitude
                    )
                    Text(
                        text = target.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Distancia: ${distance.roundToInt()}m",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun rememberFusedLocationProviderClient(): FusedLocationProviderClient {
    val context = LocalContext.current
    return remember { LocationServices.getFusedLocationProviderClient(context) }
}

suspend fun requestLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationUpdated: (Location) -> Unit
) {
    val locationRequest = Builder(1000L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMinUpdateDistanceMeters(1f)
        .build()
    
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                onLocationUpdated(location)
            }
        }
    }
    
    withContext(Dispatchers.Main) {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("ARScreen", "Error getting location updates", e)
        }
    }
}

