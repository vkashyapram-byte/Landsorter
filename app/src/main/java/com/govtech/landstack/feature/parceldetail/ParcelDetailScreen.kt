package com.govtech.landstack.feature.parceldetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelDetailScreen(
    ulpin: String,
    role: String,
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Base", "Essential", "Additional")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parcel $ulpin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> BaseLayerContent()
                    1 -> EssentialLayerContent()
                    2 -> AdditionalLayerContent()
                }
            }
        }
    }
}

@Composable
fun BaseLayerContent() {
    Column {
        Text("Base Layer", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("State: Chandigarh")
        Text("District: Chandigarh")
        Text("Village/Ward: Sector 17")
        Text("Survey Number: 17-A-1")
        Text("Parcel Type: URBAN")
        Text("Area: 500.0 sqm")
    }
}

@Composable
fun EssentialLayerContent() {
    Column {
        Text("Essential Layer", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Record of Rights", style = MaterialTheme.typography.titleMedium)
        Text("Owner: Ramesh Kumar")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Registration", style = MaterialTheme.typography.titleMedium)
        Text("Deed: REG-2020-551")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Zoning", style = MaterialTheme.typography.titleMedium)
        Text("Classification: COMMERCIAL")
    }
}

@Composable
fun AdditionalLayerContent() {
    Column {
        Text("Additional Layer", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Utilities", style = MaterialTheme.typography.titleMedium)
        Text("Water: W-CH-1001")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Taxation", style = MaterialTheme.typography.titleMedium)
        Text("Status: PAID")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Environmental", style = MaterialTheme.typography.titleMedium)
        Text("Heritage Zone: Yes")
    }
}
