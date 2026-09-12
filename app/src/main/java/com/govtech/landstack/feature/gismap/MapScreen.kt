package com.govtech.landstack.feature.gismap

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onParcelClick: (String) -> Unit,
    onNavigateToGnss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GIS Map") },
                actions = {
                    Button(onClick = onNavigateToGnss) {
                        Text("NavIC/GNSS Status")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                // MapLibre implementation goes here. 
                // For the prototype layout, we represent the map view:
                Text("MapLibre GL Map View")
            }
            
            // Search / Filter / Actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Search Parcels", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onParcelClick("CH001001") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Open Parcel CH001001")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onParcelClick("TN002002") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Open Parcel TN002002")
                    }
                }
            }
        }
    }
}
