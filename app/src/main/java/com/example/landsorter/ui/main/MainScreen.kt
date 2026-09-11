package com.example.landsorter.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.landsorter.LocationEntry
import com.example.landsorter.SupabaseClient
import com.google.android.gms.location.LocationServices
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("Ready") }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                statusText = "Permission granted. Fetching location..."
                fetchAndSaveLocation(fusedLocationClient, scope) { status ->
                    statusText = status
                }
            } else {
                statusText = "Location permission denied."
            }
        }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Landsorter GPS", modifier = Modifier.padding(bottom = 16.dp))
        
        Button(onClick = {
            val hasFineLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                statusText = "Fetching location..."
                fetchAndSaveLocation(fusedLocationClient, scope) { status ->
                    statusText = status
                }
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }) {
            Text("Sort Land (Log Location)")
        }

        Text(text = statusText, modifier = Modifier.padding(top = 16.dp))
    }
}

@SuppressLint("MissingPermission")
private fun fetchAndSaveLocation(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    scope: kotlinx.coroutines.CoroutineScope,
    onStatusUpdate: (String) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            val entry = LocationEntry(
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = Instant.now().toString(),
                description = "Manual Log"
            )
            onStatusUpdate("Location: \${location.latitude}, \${location.longitude}. Saving to Supabase...")

            scope.launch {
                try {
                    SupabaseClient.client.postgrest["locations"].insert(entry)
                    onStatusUpdate("Successfully saved location to Supabase!")
                } catch (e: Exception) {
                    onStatusUpdate("Error saving to Supabase: \${e.message}")
                }
            }
        } else {
            onStatusUpdate("Location is null. Make sure GPS is enabled on the device.")
        }
    }.addOnFailureListener { e ->
        onStatusUpdate("Failed to get location: \${e.message}")
    }
}
