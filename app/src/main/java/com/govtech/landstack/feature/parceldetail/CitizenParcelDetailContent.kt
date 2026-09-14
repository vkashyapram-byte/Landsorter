package com.govtech.landstack.feature.parceldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.govtech.landstack.data.model.Parcel
import com.govtech.landstack.data.local.OwnerEntity
import com.govtech.landstack.data.local.EncumbranceEntity
import com.govtech.landstack.data.local.DisputeEntity
import com.govtech.landstack.data.local.RestrictionEntity
import com.govtech.landstack.ui.components.SectionCard
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

// =====================================================
// Citizen Layout
// =====================================================

@Composable
fun CitizenParcelDetailContent(
    parcel: Parcel,
    owners: List<OwnerEntity>,
    encumbrances: List<EncumbranceEntity>,
    disputes: List<DisputeEntity>,
    restrictions: List<RestrictionEntity>,
    viewModel: ParcelDetailViewModel,
    ulpin: String
) {
    val context = LocalContext.current

    // GIS & UPIN
    SectionCard(title = "GIS & Location (UPIN: $ulpin)") {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Text("Map Thumbnail (GIS Points)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        DetailRow("State", parcel.base.state)
        DetailRow("District", parcel.base.district)
        DetailRow("Village/Ward", parcel.base.villageWard)
    }

    // Record of Rights & Ownership Buttons
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { Toast.makeText(context, "Record of Rights Screen Not Implemented", Toast.LENGTH_SHORT).show() }, 
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text("View Record of Rights")
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { Toast.makeText(context, "Ownership Screen Not Implemented", Toast.LENGTH_SHORT).show() }, 
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text("View Ownership & History")
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { Toast.makeText(context, "Building Permissions Screen Not Implemented", Toast.LENGTH_SHORT).show() }, 
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text("View Building Permissions")
    }
    Spacer(modifier = Modifier.height(16.dp))

    // Land Use
    SectionCard(title = "Use (Zoning & Restrictions)") {
        DetailRow("Current Land Use", parcel.base.parcelType)
        DetailRow("Master Plan Zoning", parcel.essential.zoning.classification)
        
        if (restrictions.isEmpty()) {
            Text("No development restrictions.", style = MaterialTheme.typography.bodyMedium)
        } else {
            restrictions.forEach { restriction ->
                DetailRow("Restriction (${restriction.restrictionType})", restriction.description ?: "N/A")
            }
        }
    }
}

