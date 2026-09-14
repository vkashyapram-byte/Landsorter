package com.govtech.landstack.feature.gismap

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.govtech.landstack.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.govtech.landstack.BuildConfig
import com.govtech.landstack.data.model.Parcel
import kotlinx.serialization.json.Json
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    role: String,
    onBack: () -> Unit,
    onNavigateToParcel: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val parcelsEntity by viewModel.parcels.collectAsState()
    val context = LocalContext.current

    val json = remember { Json { ignoreUnknownKeys = true } }

    val parcels = remember(parcelsEntity) {
        parcelsEntity.mapNotNull { entity ->
            try {
                val parsed = json.decodeFromString<Parcel>(entity.parcelDataJson)
                parsed
            } catch (e: Exception) {
                null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.text_parcel_map)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            factory = { ctx ->
                // Setup configuration BEFORE creating MapView
                Configuration.getInstance().load(
                    context,
                    context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
                )
                Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

                MapView(ctx).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                }
            },
            update = { view ->
                view.overlays.clear()
                var firstPoint: GeoPoint? = null

                parcels.forEach { parcel ->
                    val exteriorRing = parcel.base.geometry.coordinates.firstOrNull()
                    if (exteriorRing != null && exteriorRing.isNotEmpty()) {
                        val geoPoints = mutableListOf<GeoPoint>()
                        exteriorRing.forEach { point ->
                            if (point.size >= 2) {
                                // GeoJSON coords are [longitude, latitude]
                                geoPoints.add(GeoPoint(point[1], point[0]))
                            }
                        }
                        
                        if (geoPoints.isNotEmpty()) {
                            if (firstPoint == null) {
                                firstPoint = geoPoints.first()
                            }
                            
                            // Draw Polygon
                            val polygon = org.osmdroid.views.overlay.Polygon(view)
                            polygon.points = geoPoints
                            polygon.title = "ULPIN: ${parcel.base.ulpin}"
                            polygon.fillPaint.color = android.graphics.Color.argb(60, 99, 102, 241) // Transparent Indigo
                            polygon.outlinePaint.color = android.graphics.Color.rgb(99, 102, 241)
                            polygon.outlinePaint.strokeWidth = 4f
                            polygon.setOnClickListener { _, _, _ ->
                                onNavigateToParcel(parcel.base.ulpin)
                                true
                            }
                            view.overlays.add(polygon)

                            // Keep the marker at the first point (or a centroid if we calculated one)
                            val marker = Marker(view)
                            marker.position = geoPoints.first()
                            marker.title = "ULPIN: ${parcel.base.ulpin}"
                            marker.setOnMarkerClickListener { _, _ ->
                                onNavigateToParcel(parcel.base.ulpin)
                                true
                            }
                            view.overlays.add(marker)
                        }
                    }
                }

                if (firstPoint != null) {
                    view.controller.setCenter(firstPoint)
                }
                view.invalidate()
            }
        )
    }
}
