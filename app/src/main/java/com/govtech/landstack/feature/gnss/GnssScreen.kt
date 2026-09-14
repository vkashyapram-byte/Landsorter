package com.govtech.landstack.feature.gnss

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.govtech.landstack.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GnssScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.text_navic_gnss_status)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (hasLocationPermission) {
                GnssStatusView(context)
            } else {
                Text(stringResource(R.string.text_location_permission_is_required))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                    Text(stringResource(R.string.text_request_permission))
                }
            }
        }
    }
}

@Composable
fun GnssStatusView(context: Context) {
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    var navicSatelliteCount by remember { mutableIntStateOf(0) }
    var totalSatelliteCount by remember { mutableIntStateOf(0) }

    DisposableEffect(locationManager) {
        val gnssCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                var irnssCount = 0
                for (i in 0 until status.satelliteCount) {
                    if (status.getConstellationType(i) == GnssStatus.CONSTELLATION_IRNSS) {
                        irnssCount++
                    }
                }
                navicSatelliteCount = irnssCount
                totalSatelliteCount = status.satelliteCount
            }
        }
        
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    locationManager.registerGnssStatusCallback(context.mainExecutor, gnssCallback)
                }
            }
        } catch (e: SecurityException) {
            // Permission revoked
        }

        onDispose {
            locationManager.unregisterGnssStatusCallback(gnssCallback)
        }
    }

    Column {
        Text(stringResource(R.string.text_gnss_hardware_status), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.text_total_satellites_in_view), style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.text_navic_irnss_satellites_navicsatellitecount), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }

        if (navicSatelliteCount == 0) {
            Text(stringResource(R.string.text_no_navic_satellites_detected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(stringResource(R.string.text_navic_satellites_are_contributing))
        }
    }
}
