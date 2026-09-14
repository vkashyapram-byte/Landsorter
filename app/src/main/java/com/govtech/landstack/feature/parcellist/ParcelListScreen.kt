package com.govtech.landstack.feature.parcellist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.govtech.landstack.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.govtech.landstack.data.local.ParcelEntity
import com.govtech.landstack.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelListScreen(
    role: String,
    onBack: () -> Unit,
    onNavigateToParcel: (String) -> Unit,
    viewModel: ParcelListViewModel = hiltViewModel()
) {
    val parcelsState by viewModel.parcels.collectAsState()

    var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    
    val filteredParcels = when (val state = parcelsState) {
        is com.govtech.landstack.ui.util.UiState.Success -> {
            if (searchQuery.isBlank()) state.data else state.data.filter {
                it.ulpin.contains(searchQuery, ignoreCase = true) ||
                it.state.contains(searchQuery, ignoreCase = true) ||
                it.district.contains(searchQuery, ignoreCase = true)
            }
        }
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (role == "Citizen") "Parcel Search" else "Parcels") },
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
        },
        floatingActionButton = {
            if (role == "Land Officer" || role == "Admin") {
                FloatingActionButton(
                    onClick = { onNavigateToParcel("new") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Parcel")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.text_search_by_ulpin_or)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            when (val state = parcelsState) {
                is com.govtech.landstack.ui.util.UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is com.govtech.landstack.ui.util.UiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.text_no_parcels_found), 
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
                is com.govtech.landstack.ui.util.UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.text_couldnt_load_parcels),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { /* In a real app we'd trigger a reload event here */ }) {
                                Text(stringResource(R.string.text_retry))
                            }
                        }
                    }
                }
                is com.govtech.landstack.ui.util.UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (filteredParcels.isEmpty()) {
                            item {
                                Text(stringResource(R.string.text_no_parcels_found_matching), 
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                        items(filteredParcels) { parcel ->
                            ParcelCard(parcel, onClick = { onNavigateToParcel(parcel.ulpin) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParcelCard(parcel: ParcelEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parcel.ulpin, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.text_state), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(parcel.state, style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text(stringResource(R.string.text_district), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(parcel.district, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
