package com.govtech.landstack.feature.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            com.govtech.landstack.ui.components.LandStackTopAppBar(
                title = "Records",
                onNavigationIconClick = onBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Public Records Directory", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            RecordCategoryCard(
                title = "Encumbrance records",
                description = "View liens, mortgages, and other claims against properties.",
                onClick = { /* TODO */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            RecordCategoryCard(
                title = "Mortgage records",
                description = "Search and view registered property mortgages.",
                onClick = { /* TODO */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            RecordCategoryCard(
                title = "Land dispute records",
                description = "View active and resolved legal disputes concerning land ownership or boundaries.",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun RecordCategoryCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View")
        }
    }
}
