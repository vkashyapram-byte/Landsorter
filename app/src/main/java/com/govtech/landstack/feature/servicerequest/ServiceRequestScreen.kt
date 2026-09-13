package com.govtech.landstack.feature.servicerequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.govtech.landstack.ui.components.SectionCard
import com.govtech.landstack.ui.components.StatusBadge

data class DummyApp(
    val type: String,
    val date: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestScreen(
    onBack: () -> Unit
) {
    var apps by remember { mutableStateOf(listOf(
        DummyApp("Mutation Request", "2026-09-10", "pending")
    )) }

    var ulpin by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Mutation") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Applications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard(title = "Submit New Application") {
                    var ulpinError by remember { mutableStateOf(false) }
                    var typeError by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = ulpin,
                        onValueChange = { ulpin = it; ulpinError = it.isBlank() },
                        label = { Text("Parcel ULPIN") },
                        isError = ulpinError,
                        supportingText = { if (ulpinError) Text("ULPIN cannot be blank") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = { selectedType = it; typeError = it.isBlank() },
                        label = { Text("Application Type") },
                        isError = typeError,
                        supportingText = { if (typeError) Text("Application type cannot be blank") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Button(
                        onClick = {
                            ulpinError = ulpin.isBlank()
                            typeError = selectedType.isBlank()
                            if (!ulpinError && !typeError) {
                                apps = listOf(DummyApp("$selectedType Request", "Just Now", "submitted")) + apps
                                ulpin = ""
                                selectedType = "Mutation"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Submit Application")
                    }
                }
            }

            item {
                Text("Recent Applications", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }

            items(apps) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(app.type, style = MaterialTheme.typography.titleMedium)
                            Text(app.date, style = MaterialTheme.typography.bodySmall)
                        }
                        StatusBadge(status = app.status)
                    }
                }
            }
        }
    }
}
