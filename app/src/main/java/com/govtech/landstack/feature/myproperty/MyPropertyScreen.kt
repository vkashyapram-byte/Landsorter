package com.govtech.landstack.feature.myproperty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.govtech.landstack.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.govtech.landstack.data.local.ParcelEntity
import com.govtech.landstack.data.model.Parcel
import com.govtech.landstack.ui.util.UiState
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPropertyScreen(
    onBack: () -> Unit,
    onNavigateToParcel: (String) -> Unit,
    viewModel: MyPropertyViewModel = hiltViewModel()
) {
    val parcelsState by viewModel.parcels.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.text_my_property)) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = parcelsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.text_you_dont_have_any), 
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.text_couldnt_load_your_properties),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data) { parcel ->
                            MyPropertyCard(parcel, onClick = { onNavigateToParcel(parcel.ulpin) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyPropertyCard(parcel: ParcelEntity, onClick: () -> Unit) {
    val parcelData = try {
        Json { ignoreUnknownKeys = true }.decodeFromString<Parcel>(parcel.parcelDataJson)
    } catch (e: Exception) {
        null
    }
    val firstCoord = parcelData?.base?.geometry?.coordinates?.firstOrNull()?.firstOrNull()
    val gisText = if (firstCoord != null && firstCoord.size >= 2) {
        "Lat/Lng approx: ${String.format("%.4f", firstCoord[1])}, ${String.format("%.4f", firstCoord[0])}"
    } else {
        "GIS data unavailable"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ULPIN: ${parcel.ulpin}", 
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GIS: $gisText",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
