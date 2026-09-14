package com.govtech.landstack.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.govtech.landstack.data.local.BuildingPermissionEntity
import com.govtech.landstack.data.local.ZoningCount

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import com.govtech.landstack.ui.components.SectionCard
import com.govtech.landstack.ui.components.StatCard
import com.govtech.landstack.ui.components.StatusBadge
import com.govtech.landstack.data.model.RoleAccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    role: String,
    onNavigateToMap: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToServices: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val zoningCounts by viewModel.zoningCounts.collectAsState()
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()
    val totalParcels by viewModel.totalParcels.collectAsState()
    val activeEncumbrances by viewModel.activeEncumbrances.collectAsState()
    val totalOwners by viewModel.totalOwners.collectAsState()
    val openConflicts by viewModel.openConflicts.collectAsState()
    val allConflicts by viewModel.allConflicts.collectAsState()
    val allApplications by viewModel.allApplications.collectAsState()
    val activeApps by viewModel.activeApplications.collectAsState()

    Scaffold(
        topBar = {
            com.govtech.landstack.ui.components.LandStackTopAppBar(
                title = "Dashboard",
                onNavigationIconClick = onOpenDrawer
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Welcome, $role", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (RoleAccess.isOfficerRole(role)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard(
                                label = "Total Parcels",
                                value = totalParcels.toString(),
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard(
                                label = "Pending Approvals",
                                value = pendingApprovals.size.toString(),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard(
                                label = "Active Encumbrances",
                                value = activeEncumbrances.toString(),
                                color = MaterialTheme.colorScheme.errorContainer
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard(
                                label = "Total Owners",
                                value = totalOwners.toString(),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard(
                                label = "Data Conflicts",
                                value = openConflicts.toString(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Box(modifier = Modifier.clickable { onNavigateToServices() }.weight(1f)) {
                            StatCard(
                                label = "Active Apps",
                                value = activeApps.toString(),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        }
                    }
                }
            } else {
                // Citizen View
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.clickable { onNavigateToList() }.weight(1f)) {
                            StatCard(
                                label = "My Parcels",
                                value = totalParcels.toString(),
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                        Box(modifier = Modifier.clickable { onNavigateToServices() }.weight(1f)) {
                            StatCard(
                                label = "My Applications",
                                value = activeApps.toString(),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    if (RoleAccess.isOfficerRole(role)) {
                        Button(onClick = onNavigateToList, modifier = Modifier.weight(1f)) {
                            Text("Search & Edit Parcels")
                        }
                    }
                    Button(onClick = onNavigateToMap, modifier = Modifier.weight(1f)) {
                        Text("View on Map")
                    }
                }
            }

            item {
                SectionCard(title = "Zoning Distribution") {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)) {
                        ZoningDonutChart(zoningCounts)
                    }
                }
            }

            if (RoleAccess.isOfficerRole(role)) {
                item {
                    SectionCard(title = "Open Data Conflicts") {
                        if (allConflicts.isEmpty()) {
                            Text("No open conflicts.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                allConflicts.filter { it.status == "open" }.take(5).forEach { conflict ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(conflict.ulpin, style = MaterialTheme.typography.titleSmall)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Category: ${conflict.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                Text("Severity: ${conflict.severity}", style = MaterialTheme.typography.bodySmall, color = if (conflict.severity == "high") MaterialTheme.colorScheme.error else Color.Gray)
                                            }
                                            StatusBadge(status = conflict.status)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    SectionCard(title = "Pending Approvals") {
                        if (pendingApprovals.isEmpty()) {
                            Text("No pending approvals.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pendingApprovals.forEach { permit ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(permit.ulpin, style = MaterialTheme.typography.titleSmall)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(permit.createdAt?.toString() ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            StatusBadge(status = permit.status)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Citizen Application List
                item {
                    SectionCard(title = "My Applications") {
                        if (allApplications.isEmpty()) {
                            Text("No applications found.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                allApplications.take(5).forEach { app ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("${app.applicationType} - ${app.ulpin}", style = MaterialTheme.typography.titleSmall)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(app.createdAt.toString(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            StatusBadge(status = app.status)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ZoningDonutChart(data: List<ZoningCount>) {
    if (data.isEmpty()) {
        Text("No zoning data available.", modifier = Modifier.padding(16.dp))
        return
    }
    
    val total = data.sumOf { it.count }.toFloat()
    if (total == 0f) {
        Text("No zoning data available.", modifier = Modifier.padding(16.dp))
        return
    }
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFF59E0B),
        Color(0xFF10B981)
    )
    
    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(16.dp)) {
            var startAngle = -90f
            val strokeWidth = 40.dp.toPx()
            
            data.forEachIndexed { index, item ->
                val sweepAngle = (item.count / total) * 360f
                val color = colors[index % colors.size]
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }
        }
        
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.forEachIndexed { index, item ->
                val percentage = ((item.count / total) * 100).toInt()
                val color = colors[index % colors.size]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(12.dp), shape = RoundedCornerShape(6.dp), color = color) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${item.zoningClassification} ($percentage%)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
