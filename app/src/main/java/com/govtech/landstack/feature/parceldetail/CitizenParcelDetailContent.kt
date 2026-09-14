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

    // Record of Rights & Ownership
    SectionCard(title = "Record of Rights") {
        val currentOwners = owners.filter { it.effectiveTo == null }
        if (currentOwners.isEmpty()) {
            Text("No current owners registered.")
        } else {
            currentOwners.forEach { owner ->
                DetailRow("Owner", "${owner.ownerName} (${owner.ownershipShare ?: 0.0}%)")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        var showHistory by remember { mutableStateOf(false) }
        Button(onClick = { showHistory = !showHistory }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showHistory) "Hide Ownership History" else "View Ownership Records & History")
        }
        if (showHistory) {
            Spacer(modifier = Modifier.height(8.dp))
            OwnershipHistoryTab(owners)
        }
    }

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

    // Building Permissions
    SectionCard(title = "Building (Permissions & Approvals)") {
        DetailRow("Sanction Number", parcel.essential.building.sanctionNumber ?: "N/A")
        DetailRow("Approved Built-up Area", parcel.essential.building.approvedBuiltUpArea?.toString() ?: "N/A")
        DetailRow("Occupancy Status", parcel.essential.building.occupancyStatus ?: "N/A")
    }
}

