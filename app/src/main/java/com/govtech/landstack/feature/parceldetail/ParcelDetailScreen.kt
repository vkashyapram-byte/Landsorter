package com.govtech.landstack.feature.parceldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.govtech.landstack.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.govtech.landstack.data.local.*
import com.govtech.landstack.data.model.AccessLevel
import com.govtech.landstack.data.model.Parcel
import com.govtech.landstack.data.model.RoleAccess
import com.govtech.landstack.ui.components.SectionCard
import com.govtech.landstack.ui.components.StatusBadge
import com.govtech.landstack.data.repository.RemoteAuditLog

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelDetailScreen(
    ulpin: String,
    role: String,
    viewModel: ParcelDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    LaunchedEffect(ulpin) {
        viewModel.loadParcel(ulpin)
    }

    val parcelState by viewModel.parcelState.collectAsState()
    val pendingConflict by viewModel.pendingConflict.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val disputes by viewModel.disputes.collectAsState()
    val owners by viewModel.owners.collectAsState()
    val registrations by viewModel.registrations.collectAsState()
    val encumbrances by viewModel.encumbrances.collectAsState()
    val taxRecords by viewModel.taxRecords.collectAsState()
    val anomaly by viewModel.hasRegistrationAnomaly.collectAsState(initial = false)
    val mutationLogs by viewModel.mutationLogs.collectAsState()
    val restrictions by viewModel.restrictions.collectAsState()

    // Build visible tabs based on role
    val allTabs = if (role == "Citizen") {
        listOf(
            "Base" to RoleAccess.PARCEL_ID,
            "Ownership" to RoleAccess.PARCEL_ID,
            "Records" to RoleAccess.PARCEL_ID,
            "Building Permissions" to RoleAccess.PARCEL_ID,
            "Taxes" to RoleAccess.PARCEL_ID,
            "Application History" to RoleAccess.PARCEL_ID
        )
    } else {
        listOf(
            "Overview" to RoleAccess.PARCEL_ID,
            "Ownership" to RoleAccess.OWNERSHIP_RECORDS,
            "History" to RoleAccess.OWNERSHIP_HISTORY,
            "Registration" to RoleAccess.LAND_REGISTRATION,
            "Transactions" to RoleAccess.TRANSACTION_TRACKING,
            "Legal" to RoleAccess.ENCUMBRANCE_RECORDS,
            "Planning" to RoleAccess.RECORD_OF_RIGHTS,
            "Utilities" to RoleAccess.PARCEL_ID,
            "Documents" to RoleAccess.DOCUMENTS
        )
    }
    val visibleTabs = allTabs.filter { (_, feature) -> role == "Citizen" || RoleAccess.canView(role, feature) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            com.govtech.landstack.ui.components.LandStackTopAppBar(
                title = "Parcel $ulpin",
                onNavigationIconClick = onBack,
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        when (val state = parcelState) {
            is com.govtech.landstack.ui.util.UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }
            is com.govtech.landstack.ui.util.UiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.text_no_parcel_found))
                }
                return@Scaffold
            }
            is com.govtech.landstack.ui.util.UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.text_error_statemessage), color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadParcel(ulpin) }) { Text(stringResource(R.string.text_retry)) }
                    }
                }
                return@Scaffold
            }
            is com.govtech.landstack.ui.util.UiState.Success -> {
                // proceed
            }
        }
        val parcel = (parcelState as com.govtech.landstack.ui.util.UiState.Success).data

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if ("Citizen".equals(role, ignoreCase = true)) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CitizenParcelDetailContent(parcel, owners, encumbrances, disputes, restrictions, viewModel, ulpin)
                }
            } else {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 8.dp
                ) {
                    visibleTabs.forEachIndexed { index, tabInfo ->
                        val title = tabInfo.first
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier.verticalScroll(scrollState).fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val currentTab = visibleTabs.getOrNull(selectedTabIndex)?.first ?: "Overview"
                        when (currentTab) {
                            "Overview", "Base" -> OverviewTabContent(
                                parcel = parcel,
                                hasDisputes = disputes.any { it.status == "active" },
                                hasEncumbrances = encumbrances.any { it.resolvedAt == null && it.lienStatus?.lowercase() == "active" },
                                hasAnomalies = anomaly
                            )
                            "Ownership" -> OwnershipRecordsTab(owners, role, mutationLogs, viewModel, ulpin)
                            "History" -> OwnershipHistoryTab(owners)
                            "Registration" -> RegistrationTab(registrations, documents, role)
                            "Transactions" -> TransactionTrackingTab(registrations)
                            "Legal", "Records" -> LegalTabContent(encumbrances, disputes, restrictions, role)
                            "Planning" -> PlanningTabContent(parcel)
                            "Utilities" -> UtilitiesTabContent(parcel)
                            "Documents" -> DocumentsTabContent(documents, viewModel)
                            "Building Permissions" -> BuildingPermissionsTabContent() // stub
                            "Taxes" -> TaxesTabContent(taxRecords)
                            "Application History" -> ApplicationHistoryTabContent() // stub
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// Detail Row component
// =====================================================

@Composable
fun DetailRow(label: String, value: String, customValue: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        if (customValue != null) {
            Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.CenterEnd) {
                customValue()
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.End
            )
        }
    }
}

// =====================================================
// Tab 1: Overview
// =====================================================

@Composable
fun OverviewTabContent(
    parcel: Parcel,
    hasDisputes: Boolean,
    hasEncumbrances: Boolean,
    hasAnomalies: Boolean
) {
    var showVerificationResult by remember { mutableStateOf(false) }

    SectionCard(title = "Location Data") {
        DetailRow("State", parcel.base.state)
        DetailRow("District", parcel.base.district)
        if (parcel.base.taluk != null) {
            DetailRow("Taluk", parcel.base.taluk)
        }
        DetailRow("Village/Ward", parcel.base.villageWard)
        DetailRow("Survey Number", parcel.base.surveyNumber)
    }

    SectionCard(title = "Parcel Details") {
        DetailRow("Parcel Type", parcel.base.parcelType)
        DetailRow("Area (sqm)", parcel.base.areaSqm.toString())
    }

    SectionCard(title = "Property Verification") {
        Button(
            onClick = { showVerificationResult = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text(stringResource(R.string.text_run_property_verification))
        }

        if (showVerificationResult) {
            val isClear = !hasDisputes && !hasEncumbrances && !hasAnomalies
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isClear) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isClear) "✓" else "⚠", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isClear) "Property is Clear" else "Issues Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!isClear) {
                        if (hasDisputes) Text(stringResource(R.string.text_active_legal_disputes_found))
                        if (hasEncumbrances) Text(stringResource(R.string.text_financial_encumbrancesliens_found))
                        if (hasAnomalies) Text(stringResource(R.string.text_registration_anomalies_detected_multiple))
                    } else {
                        Text(stringResource(R.string.text_no_active_disputes_encumbrances))
                    }
                }
            }
        }
    }
}

// =====================================================
// Tab 2: Ownership Records (current owners)
// =====================================================

@Composable
fun OwnershipRecordsTab(
    owners: List<OwnerEntity>,
    role: String,
    mutationLogs: List<RemoteAuditLog>,
    viewModel: ParcelDetailViewModel,
    ulpin: String
) {
    val currentOwners = owners.filter { it.effectiveTo == null }

    if (currentOwners.isEmpty()) {
        SectionCard(title = "Current Owners") {
            Text(stringResource(R.string.text_no_ownership_records_found), style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        SectionCard(title = "Current Owners") {
            currentOwners.forEach { owner ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = owner.ownerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = owner.rightType ?: "Individual",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            StatusBadge(status = "verified")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("Khata Number", owner.khataNumber ?: "N/A")
                        DetailRow("Ownership Share", "${((owner.ownershipShare ?: 1.0) * 100).toInt()}%")
                        DetailRow("Effective From", owner.effectiveFrom.toString().take(10))
                    }
                }
            }
        }
    }

    if (RoleAccess.canEdit(role, RoleAccess.OWNERSHIP_RECORDS)) {
        SectionCard(title = "Record Mutation (Admin/Registrar)") {
            var newOwner by remember { mutableStateOf("") }
            var userEmail by remember { mutableStateOf("") }
            var khata by remember { mutableStateOf("") }
            var rightType by remember { mutableStateOf("Individual") }
            var shareStr by remember { mutableStateOf("1.0") }

            var ownerError by remember { mutableStateOf(false) }
            var khataError by remember { mutableStateOf(false) }
            var shareError by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = newOwner,
                onValueChange = { newOwner = it; ownerError = it.isBlank() },
                label = { Text(stringResource(R.string.text_new_owner_name)) },
                isError = ownerError,
                supportingText = { if (ownerError) Text(stringResource(R.string.text_owner_name_cannot_be)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = userEmail,
                onValueChange = { userEmail = it },
                label = { Text(stringResource(R.string.text_user_email_optional_to)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = khata,
                onValueChange = { khata = it; khataError = it.isBlank() },
                label = { Text(stringResource(R.string.text_khata_number)) },
                isError = khataError,
                supportingText = { if (khataError) Text(stringResource(R.string.text_khata_number_cannot_be)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = rightType,
                onValueChange = { rightType = it },
                label = { Text(stringResource(R.string.text_right_type_eg_individual)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = shareStr,
                onValueChange = { 
                    shareStr = it 
                    val d = it.toDoubleOrNull()
                    shareError = d == null || d <= 0.0 || d > 1.0
                },
                label = { Text(stringResource(R.string.text_share_eg_10_or)) },
                isError = shareError,
                supportingText = { if (shareError) Text(stringResource(R.string.text_share_must_be_a)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Button(
                onClick = {
                    ownerError = newOwner.isBlank()
                    khataError = khata.isBlank()
                    val d = shareStr.toDoubleOrNull()
                    shareError = d == null || d <= 0.0 || d > 1.0
                    
                    if (!ownerError && !khataError && !shareError) {
                        viewModel.submitOwnershipMutation(ulpin, newOwner, khata, rightType, d ?: 1.0, userEmail.ifBlank { null })
                        newOwner = ""
                        userEmail = ""
                        khata = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.text_submit_mutation))
            }
        }
    }

    if (mutationLogs.isNotEmpty() && RoleAccess.isOfficerRole(role)) {
        SectionCard(title = "Mutation Audit Log") {
            mutationLogs.sortedByDescending { it.timestamp }.forEach { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.text_action_logaction), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.text_actor_role_loguserrole), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(stringResource(R.string.text_timestamp_javatimeinstantofepochmillilogtimestamp), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// =====================================================
// Tab 3: Ownership History (timeline)
// =====================================================

@Composable
fun OwnershipHistoryTab(owners: List<OwnerEntity>) {
    val sorted = owners.sortedBy { it.effectiveFrom }

    if (sorted.isEmpty()) {
        SectionCard(title = "Ownership History") {
            Text(stringResource(R.string.text_no_ownership_history_available), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    SectionCard(title = "Ownership Timeline") {
        sorted.forEachIndexed { index, owner ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Timeline indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (owner.effectiveTo == null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                    if (index < sorted.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = owner.ownerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (owner.effectiveTo == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (owner.effectiveTo == null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                    )
                    val fromYear = owner.effectiveFrom.toString().take(4)
                    val toYear = owner.effectiveTo?.toString()?.take(4) ?: "Present"
                    Text(
                        text = "$fromYear → $toYear",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (owner.rightType != null) {
                        Text(
                            text = owner.rightType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                if (owner.effectiveTo == null) {
                    StatusBadge(status = "active")
                }
            }
        }
    }
}

// =====================================================
// Tab 4: Land Registration
// =====================================================

@Composable
fun RegistrationTab(
    registrations: List<RegistrationEntity>,
    documents: List<DocumentEntity>,
    role: String
) {
    if (registrations.isEmpty()) {
        SectionCard(title = "Land Registrations") {
            Text(stringResource(R.string.text_no_registration_records_found), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val sortedRegs = registrations.sortedByDescending { it.registrationDate }
    val latest = sortedRegs.first()

    SectionCard(title = "Registration Record") {
        DetailRow("Registration Date", latest.registrationDate ?: "N/A")
        DetailRow("Deed Number", latest.deedNumber ?: "N/A")
        DetailRow("Transaction Type", latest.transactionType ?: "N/A") {
            StatusBadge(status = latest.transactionType ?: "unknown")
        }
        DetailRow("Status", "Registered") {
            StatusBadge(status = "verified")
        }
        // Note about the faked status
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.text_), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Note: 'Status: Registered' is inferred. The underlying schema does not explicitly model pending/complete sale states.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    SectionCard(title = "Previous Transaction History") {
        sortedRegs.forEachIndexed { index, reg ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Registration ${reg.deedNumber ?: "N/A"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = reg.registrationDate ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Type: ${reg.transactionType ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    val matchingDoc = documents.find { it.registrationId == reg.id }
                    if (matchingDoc != null) {
                        StatusBadge(status = matchingDoc.verificationStatus)
                    }
                }
            }
            if (index < sortedRegs.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

// =====================================================
// Tab 5: Transaction Tracking (step tracker)
// =====================================================

@Composable
fun TransactionTrackingTab(registrations: List<RegistrationEntity>) {
    if (registrations.isEmpty()) {
        SectionCard(title = "Transaction Status") {
            Text(stringResource(R.string.text_no_active_transactions), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    // Use the most recent registration as the "active" transaction
    val latest = registrations.first()

    SectionCard(title = "Transaction Tracker — ${latest.deedNumber ?: "N/A"}") {
        val steps = listOf(
            "Application Submitted",
            "Documents Verified",
            "Fee Paid",
            "Officer Review",
            "Registration",
            "Certificate Issued"
        )

        // Derive step completion from the transaction type / existence of data
        // A simple heuristic: if we have a deed number and date, early steps are done
        val completedSteps = when {
            latest.stampDutyPaid != null && latest.stampDutyPaid > 0 &&
                latest.registrationDate != null -> 5 // nearly complete
            latest.stampDutyPaid != null && latest.stampDutyPaid > 0 -> 3
            latest.deedNumber != null -> 1
            else -> 0
        }

        steps.forEachIndexed { index, step ->
            val icon = when {
                index < completedSteps -> "✓"
                index == completedSteps -> "⏳"
                else -> "○"
            }
            val iconColor = when {
                index < completedSteps -> Color(0xFF10B981) // green
                index == completedSteps -> Color(0xFFF59E0B) // amber
                else -> Color.Gray
            }
            val textColor = when {
                index < completedSteps -> MaterialTheme.colorScheme.onSurface
                index == completedSteps -> MaterialTheme.colorScheme.onSurface
                else -> Color.Gray
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = icon,
                            color = iconColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    fontWeight = if (index == completedSteps) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Connecting line between steps
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .width(2.dp)
                        .height(8.dp)
                        .background(
                            if (index < completedSteps) Color(0xFF10B981).copy(alpha = 0.4f)
                            else Color.Gray.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}

// =====================================================
// Tab 6: Legal / Encumbrances
// =====================================================

@Composable
fun LegalTabContent(
    encumbrances: List<EncumbranceEntity>,
    disputes: List<DisputeEntity>,
    restrictions: List<RestrictionEntity>,
    role: String
) {
    if (encumbrances.isEmpty() && disputes.isEmpty() && restrictions.isEmpty()) {
        SectionCard(title = "Legal Status") {
            Text(stringResource(R.string.text_no_encumbrances_disputes_or), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    if (restrictions.isNotEmpty()) {
        SectionCard(title = "Restrictions & Court Orders") {
            restrictions.forEachIndexed { index, restriction ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    DetailRow("Type", restriction.restrictionType.uppercase()) {
                        StatusBadge(if (restriction.effectiveTo == null) "rejected" else "resolved")
                    }
                    if (restriction.description != null) {
                        DetailRow("Description", restriction.description)
                    }
                    DetailRow("Source / Authority", restriction.source)
                    if (restriction.effectiveFrom != null) {
                        DetailRow("Effective From", restriction.effectiveFrom)
                    }
                }
                if (index < restrictions.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }

    if (disputes.isNotEmpty()) {
        SectionCard(title = "Disputes & Litigation") {
            disputes.forEachIndexed { index, disp ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    DetailRow("Type", disp.disputeType.uppercase()) {
                        StatusBadge(if (disp.status == "active") "rejected" else "resolved")
                    }
                    if (disp.caseNumber != null) {
                        DetailRow("Case Number", disp.caseNumber)
                    }
                    DetailRow("Parties Involved", disp.partiesInvolved)
                    DetailRow("Filed By", disp.filedBy)
                    DetailRow("Source", disp.source)
                    DetailRow("Status", disp.status)
                }
                if (index < disputes.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }

    if (encumbrances.isNotEmpty()) {
        SectionCard(title = "Financial Encumbrances") {
            encumbrances.forEachIndexed { index, enc ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    DetailRow("Lender", enc.lender ?: "N/A")
                    DetailRow("Loan Amount", "₹${enc.loanAmount ?: 0.0}")
                    DetailRow("Lien Status", enc.lienStatus ?: "N/A") {
                        StatusBadge(status = enc.lienStatus ?: "unknown")
                    }
                    if (enc.validFrom != null) {
                        DetailRow("Valid From", enc.validFrom.toString().take(10))
                    }
                    if (enc.validTo != null) {
                        DetailRow("Valid To", enc.validTo.toString().take(10))
                    }
                    if (enc.resolvedAt != null) {
                        DetailRow("Resolved At", enc.resolvedAt.toString().take(10)) {
                            StatusBadge(status = "resolved")
                        }
                    }
                }
                if (index < encumbrances.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        // Summary card for encumbrances
        val activeCount = encumbrances.count { enc -> enc.resolvedAt == null && enc.lienStatus?.lowercase() == "active" }
        if (activeCount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.text__1), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$activeCount Active Encumbrance${if (activeCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "This property has existing financial obligations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// Tab 7: Planning (existing)
// =====================================================

@Composable
fun PlanningTabContent(parcel: Parcel) {
    val zoning = parcel.essential.zoning
    SectionCard(title = "Master Plan Zoning") {
        DetailRow("Classification", zoning.classification)
        DetailRow("Permissible FSI", zoning.permissibleFsi.toString())
        DetailRow("Permitted Use", zoning.permittedUse)
    }

    val planning = parcel.essential.planning
    SectionCard(title = "Development Constraints") {
        if (planning != null) {
            DetailRow("Commercial Use", if (planning.commercialUse) "Permitted" else "Restricted") {
                StatusBadge(if (planning.commercialUse) "approved" else "rejected")
            }
            DetailRow("Industrial Use", if (planning.industrialUse) "Permitted" else "Restricted") {
                StatusBadge(if (planning.industrialUse) "approved" else "rejected")
            }
            DetailRow("Road Reservation", if (planning.roadReservation) "Yes" else "No")
            DetailRow("Development Zone", planning.developmentZone)
        } else {
            DetailRow("Data", "No planning info available")
        }
    }
}

// =====================================================
// Tab 8: Utilities (existing)
// =====================================================

@Composable
fun UtilitiesTabContent(parcel: Parcel) {
    val utils = parcel.additional.utilities
    SectionCard(title = "Utility Connections") {
        DetailRow("Water Connection", utils.waterConnectionId)
        DetailRow("Electricity Connection", utils.electricityConnectionId)
        DetailRow("Sewage Access", if (utils.sewageAccess) "Yes" else "No")
        DetailRow("Road Access", utils.roadAccess)
    }
}

// =====================================================
// Tab 9: Documents (existing)
// =====================================================

@Composable
fun DocumentsTabContent(documents: List<DocumentEntity>, viewModel: ParcelDetailViewModel) {
    if (documents.isEmpty()) {
        Text(stringResource(R.string.text_no_documents_found), modifier = Modifier.padding(16.dp))
        return
    }
    documents.forEach { doc ->
        SectionCard(title = doc.docType.uppercase()) {
            DetailRow("Verification Status", doc.verificationStatus) {
                StatusBadge(doc.verificationStatus)
            }
            DetailRow("Heuristic Check", viewModel.verifyDocumentHeuristic(doc)) {
                val check = viewModel.verifyDocumentHeuristic(doc)
                if (check.startsWith("⚠")) {
                    Badge(containerColor = MaterialTheme.colorScheme.error) { Text(stringResource(R.string.text__1)) }
                } else {
                    Badge(containerColor = Color(0xFF4CAF50)) { Text(stringResource(R.string.text__2)) }
                }
            }
            DetailRow("Uploaded By", doc.uploadedBy)
            DetailRow("Extracted Text", doc.ocrExtractedText?.take(50) ?: "None")
        }
    }
}

@Composable
fun BuildingPermissionsTabContent() {
    SectionCard(title = "Building Permissions") {
        Text(stringResource(R.string.text_coming_soon))
    }
}

@Composable
fun TaxesTabContent(taxRecords: List<TaxRecordEntity>) {
    if (taxRecords.isEmpty()) {
        SectionCard(title = "Tax Records") {
            Text(stringResource(R.string.text_no_tax_records_found))
        }
        return
    }

    SectionCard(title = "Property Taxes") {
        taxRecords.sortedByDescending { it.taxYear }.forEach { tax ->
            DetailRow("Year", tax.taxYear?.toString() ?: "N/A")
            DetailRow("Annual Tax", "₹${tax.annualTax ?: 0.0}")
            DetailRow("Status", tax.paymentStatus ?: "Unknown") {
                StatusBadge(tax.paymentStatus ?: "unknown")
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun ApplicationHistoryTabContent() {
    SectionCard(title = "Application History") {
        Text(stringResource(R.string.text_coming_soon))
    }
}
