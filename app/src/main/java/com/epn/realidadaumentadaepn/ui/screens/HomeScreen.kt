package com.epn.realidadaumentadaepn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.location.Location
import com.epn.realidadaumentadaepn.data.EPNLocation
import com.epn.realidadaumentadaepn.data.EPNLocations
import kotlin.math.roundToInt
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import com.epn.realidadaumentadaepn.util.LocationUtils

@Composable
fun HomeScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredLocations by remember { mutableStateOf(EPNLocations.locations) }
    val currentLocation = remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = rememberFusedLocationProviderClient()

    LaunchedEffect(Unit) {
        requestLocationUpdates(fusedLocationClient) { location ->
            currentLocation.value = location
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                filteredLocations = EPNLocations.locations.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar ubicación...") },
            leadingIcon = { Icon(Icons.Default.Search, "Buscar") }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(filteredLocations) { location ->
                LocationCard(
                    location = location,
                    currentLocation = currentLocation,
                    onClick = { navController.navigate("ar/${location.name}") }
                )
            }
        }
    }
}

@Composable
fun LocationCard(
    location: EPNLocation,
    currentLocation: MutableState<Location?>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = location.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = location.description)
            currentLocation.value?.let { current ->
                val distance = LocationUtils.calculateDistance(
                    current.latitude, current.longitude,
                    location.latitude, location.longitude
                )
                Text(
                    text = "A ${distance.roundToInt()} metros",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}