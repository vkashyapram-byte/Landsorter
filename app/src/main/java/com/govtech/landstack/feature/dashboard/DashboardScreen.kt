package com.govtech.landstack.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    role: String,
    onNavigateToMap: () -> Unit,
    onNavigateToServices: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard - $role") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (role) {
                "Citizen" -> CitizenDashboard(onNavigateToMap, onNavigateToServices)
                "Land Officer", "Admin" -> OfficerDashboard(onNavigateToMap, onNavigateToServices)
            }
        }
    }
}

@Composable
fun CitizenDashboard(onNavigateToMap: () -> Unit, onNavigateToServices: () -> Unit) {
    Text("Welcome, Citizen", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(24.dp))

    Button(onClick = onNavigateToMap, modifier = Modifier.fillMaxWidth()) {
        Text("View My Parcels on Map")
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onNavigateToServices, modifier = Modifier.fillMaxWidth()) {
        Text("Request Service (e.g. Mutation)")
    }
}

@Composable
fun OfficerDashboard(onNavigateToMap: () -> Unit, onNavigateToServices: () -> Unit) {
    Text("Welcome, Officer", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(24.dp))

    Button(onClick = onNavigateToMap, modifier = Modifier.fillMaxWidth()) {
        Text("Search & View Parcels")
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onNavigateToServices, modifier = Modifier.fillMaxWidth()) {
        Text("Pending Approvals Queue")
    }

    Spacer(modifier = Modifier.height(32.dp))
    Text("Analytics", style = MaterialTheme.typography.titleMedium)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Vico Chart Placeholder (Land Use Distribution)")
        }
    }
}
