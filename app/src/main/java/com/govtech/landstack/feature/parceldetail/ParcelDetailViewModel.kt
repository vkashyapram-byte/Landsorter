package com.govtech.landstack.feature.parceldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.govtech.landstack.data.local.ParcelDao
import com.govtech.landstack.data.local.RelationalDao
import com.govtech.landstack.data.local.*
import com.govtech.landstack.data.model.*
import com.govtech.landstack.data.repository.SyncRepository
import com.govtech.landstack.data.repository.SyncResult
import com.govtech.landstack.data.repository.RemoteAuditLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

import android.content.Context
import com.govtech.landstack.data.sync.SyncManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import com.govtech.landstack.feature.auth.UserProfile
import java.time.Instant

@HiltViewModel
class ParcelDetailViewModel @Inject constructor(
    private val parcelDao: ParcelDao,
    private val relationalDao: RelationalDao,
    private val pendingConflictDao: PendingConflictDao,
    private val auditLogDao: AuditLogDao,
    private val documentDao: DocumentDao,
    private val disputeDao: DisputeDao,
    private val restrictionDao: RestrictionDao,
    private val syncRepository: SyncRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    
    private val _userRole = MutableStateFlow<String?>("Citizen")
    val userRole = _userRole.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    val profile = supabase.postgrest["profiles"]
                        .select { filter { eq("id", user.id) } }
                        .decodeSingleOrNull<UserProfile>()
                    _userRole.value = profile?.role ?: "Citizen"
                }
            } catch (e: Exception) {
                _userRole.value = "Citizen"
            }
        }
    }

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _parcelState = MutableStateFlow<com.govtech.landstack.ui.util.UiState<Parcel>>(com.govtech.landstack.ui.util.UiState.Loading)
    val parcelState = _parcelState.asStateFlow()
    
    private val _pendingConflict = MutableStateFlow<com.govtech.landstack.data.local.PendingConflictEntity?>(null)
    val pendingConflict = _pendingConflict.asStateFlow()

    private val _ulpinValidationState = MutableStateFlow<String?>(null)
    val ulpinValidationState = _ulpinValidationState.asStateFlow()

    private val _owners = MutableStateFlow<List<OwnerEntity>>(emptyList())
    val owners = _owners.asStateFlow()

    private val _permissions = MutableStateFlow<List<BuildingPermissionEntity>>(emptyList())
    val permissions = _permissions.asStateFlow()

    private val _encumbrances = MutableStateFlow<List<EncumbranceEntity>>(emptyList())
    val encumbrances = _encumbrances.asStateFlow()

    private val _taxRecords = MutableStateFlow<List<TaxRecordEntity>>(emptyList())
    val taxRecords = _taxRecords.asStateFlow()

    private val _registrations = MutableStateFlow<List<RegistrationEntity>>(emptyList())
    val registrations = _registrations.asStateFlow()

    private val _documents = MutableStateFlow<List<DocumentEntity>>(emptyList())
    val documents = _documents.asStateFlow()

    private val _disputes = MutableStateFlow<List<DisputeEntity>>(emptyList())
    val disputes = _disputes.asStateFlow()

    private val _restrictions = MutableStateFlow<List<RestrictionEntity>>(emptyList())
    val restrictions = _restrictions.asStateFlow()

    private val _mutationLogs = MutableStateFlow<List<RemoteAuditLog>>(emptyList())
    val mutationLogs = _mutationLogs.asStateFlow()

    val hasRegistrationAnomaly = _registrations.map { regsList ->
        val regs = regsList.sortedBy { it.registrationDate }
        var anomaly = false
        for (i in 0 until regs.size - 1) {
            val d1 = try { java.time.LocalDate.parse(regs[i].registrationDate ?: "") } catch(e:Exception) { null }
            val d2 = try { java.time.LocalDate.parse(regs[i+1].registrationDate ?: "") } catch(e:Exception) { null }
            if (d1 != null && d2 != null) {
                if (java.time.temporal.ChronoUnit.DAYS.between(d1, d2) <= 30) {
                    anomaly = true
                    break
                }
            }
        }
        anomaly
    }

    private var isNew = false
    private var relationalJob: Job? = null

    fun loadParcel(ulpin: String) {
        viewModelScope.launch {
            if (ulpin == "new") {
                isNew = true
                _parcelState.value = com.govtech.landstack.ui.util.UiState.Success(emptyParcel())
            } else {
                isNew = false
                val entity = parcelDao.getParcel(ulpin).firstOrNull()
                if (entity != null) {
                    try {
                        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        _parcelState.value = com.govtech.landstack.ui.util.UiState.Success(json.decodeFromString<Parcel>(entity.parcelDataJson))
                    } catch (e: Exception) {
                        _uiEvent.emit("Error decoding parcel data. Using fallback.")
                        _parcelState.value = com.govtech.landstack.ui.util.UiState.Success(emptyParcel().let { p ->
                            p.copy(
                                base = p.base.copy(
                                    ulpin = entity.ulpin,
                                    state = entity.state,
                                    district = entity.district
                                ),
                                essential = p.essential.copy(
                                    zoning = p.essential.zoning.copy(
                                        classification = entity.zoningClassification ?: "",
                                        permittedUse = entity.permittedUse ?: ""
                                    )
                                )
                            )
                        })
                    }
                } else {
                    _parcelState.value = com.govtech.landstack.ui.util.UiState.Error("Parcel not found locally")
                    return@launch
                }
                
                // Load relational data
                relationalJob?.cancel()
                relationalJob = viewModelScope.launch {
                    launch {
                        pendingConflictDao.getConflict(ulpin).collect { conflict ->
                            _pendingConflict.value = conflict
                        }
                    }
                    launch { relationalDao.getOwnersForParcel(ulpin).collect { _owners.value = it } }
                    launch { relationalDao.getPermissionsForParcel(ulpin).collect { _permissions.value = it } }
                    launch { relationalDao.getEncumbrancesForParcel(ulpin).collect { _encumbrances.value = it } }
                    launch { relationalDao.getTaxRecordsForParcel(ulpin).collect { _taxRecords.value = it } }
                    launch { relationalDao.getRegistrationsForParcel(ulpin).collect { _registrations.value = it } }
                    launch { documentDao.getDocumentsByUlpin(ulpin).collect { _documents.value = it } }
                    launch { disputeDao.getDisputesByUlpin(ulpin).collect { _disputes.value = it } }
                    launch { restrictionDao.getRestrictionsByUlpin(ulpin).collect { _restrictions.value = it } }
                }

                // Trigger delta syncs for relational tables to ensure fresh data
                launch {
                    syncRepository.pullOwners()
                    syncRepository.pullPermissions()
                    syncRepository.pullEncumbrances()
                    syncRepository.pullTaxRecords()
                    syncRepository.pullRegistrations()
                    syncRepository.pullDocuments()
                    syncRepository.pullDisputes()
                    syncRepository.pullRestrictions()
                    
                    if (RoleAccess.canView(_userRole.value ?: "Citizen", RoleAccess.RECORD_OF_RIGHTS)) {
                        _mutationLogs.value = syncRepository.fetchOwnershipMutations(ulpin)
                    }
                }
            }
        }
    }
    
    fun updateParcel(updatedParcel: Parcel) {
        _parcelState.value = com.govtech.landstack.ui.util.UiState.Success(updatedParcel)
        if (isNew) {
            _ulpinValidationState.value = null // Reset validation state on change
        }
    }

    suspend fun checkUlpinUniqueness(ulpin: String): Boolean {
        if (ulpin.isBlank()) {
            _ulpinValidationState.value = "ULPIN cannot be empty"
            return false
        }
        
        // Check local
        val localExists = parcelDao.getParcelSync(ulpin) != null
        if (localExists) {
            _ulpinValidationState.value = "A parcel with this ULPIN already exists."
            return false
        }

        // Check remote
        val remoteExists = syncRepository.checkParcelExistsRemotely(ulpin)
        if (remoteExists) {
            _ulpinValidationState.value = "A parcel with this ULPIN already exists."
            return false
        }

        _ulpinValidationState.value = "ULPIN is available"
        return true
    }

    fun onCheckUlpinClicked() {
        val currentState = _parcelState.value
        if (currentState !is com.govtech.landstack.ui.util.UiState.Success) return
        val currentParcel = currentState.data
        viewModelScope.launch {
            checkUlpinUniqueness(currentParcel.base.ulpin)
        }
    }

    fun onSaveParcelClicked() {
        val currentState = _parcelState.value
        if (currentState !is com.govtech.landstack.ui.util.UiState.Success) return
        val currentParcel = currentState.data
        
        viewModelScope.launch {
            if (isNew) {
                val isUnique = checkUlpinUniqueness(currentParcel.base.ulpin)
                if (!isUnique) {
                    _uiEvent.emit("Cannot save: ULPIN already exists")
                    return@launch
                }
            } else {
                if (currentParcel.base.ulpin.isBlank()) {
                    _uiEvent.emit("ULPIN cannot be empty")
                    return@launch
                }
            }

            // Save locally
            val entity = ParcelEntity(
                ulpin = currentParcel.base.ulpin,
                state = currentParcel.base.state,
                district = currentParcel.base.district,
                zoningClassification = currentParcel.essential.zoning.classification,
                permittedUse = currentParcel.essential.zoning.permittedUse,
                parcelDataJson = Json.encodeToString(currentParcel),
                updatedAt = Instant.now().toString()
            )
            parcelDao.insertParcels(listOf(entity))

            // Sync to remote
            val result = syncRepository.pushSingleParcel(currentParcel.base.ulpin)
            when (result) {
                is SyncResult.PermissionDenied -> {
                    _uiEvent.emit("You don't have permission to edit parcels")
                }
                is SyncResult.NetworkError -> {
                    _uiEvent.emit("Network error, sync queued")
                }
                is SyncResult.Success -> {
                    _uiEvent.emit("Parcel saved and synced successfully")
                }
                else -> {
                    _uiEvent.emit("Unknown error occurred")
                }
            }
        }
    }

    
    // -----------------------------------------
    // WRITE ACTIONS (Wired to Granular Sync)
    // -----------------------------------------

    fun approveRejectPermit(context: Context, permit: BuildingPermissionEntity, decision: String) {
        viewModelScope.launch {
            val updated = permit.copy(status = decision, decidedAt = Instant.now())
            relationalDao.insertPermissions(listOf(updated))
            SyncManager.enqueueGranularSync(context, "permit", permit.id.toString(), "decision")
            _uiEvent.emit("Permit decision recorded and sync queued")
        }
    }

    fun resolveEncumbrance(context: Context, enc: EncumbranceEntity) {
        viewModelScope.launch {
            val updated = enc.copy(resolvedAt = Instant.now())
            relationalDao.insertEncumbrances(listOf(updated))
            SyncManager.enqueueGranularSync(context, "encumbrance", enc.id.toString(), "resolve")
            _uiEvent.emit("Encumbrance resolved and sync queued")
        }
    }

    fun markTaxPaid(context: Context, tax: TaxRecordEntity) {
        viewModelScope.launch {
            val updated = tax.copy(paymentStatus = "paid", paidAt = Instant.now())
            relationalDao.insertTaxRecords(listOf(updated))
            SyncManager.enqueueGranularSync(context, "tax_record", tax.id.toString(), "mark_paid")
            _uiEvent.emit("Tax marked as paid and sync queued")
        }
    }

    fun submitPermitApplication(context: Context, ulpin: String, sanctionNumber: String, area: Double, floors: Int) {
        viewModelScope.launch {
            val tempId = -System.currentTimeMillis()
            val userId = supabase.auth.currentUserOrNull()?.id
            val newPermit = BuildingPermissionEntity(
                id = tempId, ulpin = ulpin, sanctionNumber = sanctionNumber,
                approvedBuiltUpArea = area, floors = floors, status = "applied",
                submittedBy = userId, decidedBy = null, decidedAt = null,
                createdAt = Instant.now(), updatedAt = Instant.now()
            )
            relationalDao.insertPermissions(listOf(newPermit))
            SyncManager.enqueueGranularSync(context, "permit", tempId.toString(), "insert")
            _uiEvent.emit("Permit application submitted and queued")
        }
    }

    fun addEncumbrance(context: Context, ulpin: String, lender: String, amount: Double, status: String) {
        viewModelScope.launch {
            val tempId = -System.currentTimeMillis()
            val newEnc = EncumbranceEntity(
                id = tempId, ulpin = ulpin, lender = lender, loanAmount = amount,
                lienStatus = status, validFrom = Instant.now(), validTo = null,
                resolvedAt = null, createdAt = Instant.now(), updatedAt = Instant.now()
            )
            relationalDao.insertEncumbrances(listOf(newEnc))
            SyncManager.enqueueGranularSync(context, "encumbrance", tempId.toString(), "insert")
            _uiEvent.emit("Encumbrance added and queued")
        }
    }

    fun addTaxRecord(context: Context, ulpin: String, year: Int, assessed: Double, annual: Double) {
        viewModelScope.launch {
            val tempId = -System.currentTimeMillis()
            val newTax = TaxRecordEntity(
                id = tempId, ulpin = ulpin, taxYear = year, assessedValue = assessed,
                annualTax = annual, paymentStatus = "unpaid", arrears = 0.0, paidAt = null,
                createdAt = Instant.now(), updatedAt = Instant.now()
            )
            relationalDao.insertTaxRecords(listOf(newTax))
            SyncManager.enqueueGranularSync(context, "tax_record", tempId.toString(), "insert")
            _uiEvent.emit("Tax record added and queued")
        }
    }

    fun addDispute(context: Context, ulpin: String, type: String, parties: String, filedBy: String, source: String, caseNum: String) {
        viewModelScope.launch {
            val tempId = -System.currentTimeMillis()
            val newDisp = DisputeEntity(
                id = tempId, ulpin = ulpin, disputeType = type, status = "active",
                partiesInvolved = parties, filedBy = filedBy,
                caseNumber = caseNum, description = null, source = source, 
                sourceReference = null, effectiveDate = null, isStayOrder = false,
                createdAt = Instant.now(), updatedAt = Instant.now()
            )
            disputeDao.insertDisputes(listOf(newDisp))
            SyncManager.enqueueGranularSync(context, "dispute", tempId.toString(), "insert")
            _uiEvent.emit("Dispute added and queued")
        }
    }

    fun addRestriction(context: Context, ulpin: String, type: String, description: String, source: String) {
        viewModelScope.launch {
            val tempId = -System.currentTimeMillis()
            val newRestr = RestrictionEntity(
                id = tempId, ulpin = ulpin, restrictionType = type,
                description = description, source = source,
                effectiveFrom = null, effectiveTo = null,
                createdAt = Instant.now(), updatedAt = Instant.now()
            )
            restrictionDao.insertRestrictions(listOf(newRestr))
            SyncManager.enqueueGranularSync(context, "parcel_restriction", tempId.toString(), "insert")
            _uiEvent.emit("Restriction added and queued")
        }
    }

    fun addRegistration(context: Context, ulpin: String, deed: String, type: String, stampDuty: Double, subOffice: String) {
        viewModelScope.launch {
            val tempId = -System.currentTimeMillis()
            val newReg = RegistrationEntity(
                id = tempId, ulpin = ulpin, deedNumber = deed, registrationDate = Instant.now().toString(),
                transactionType = type, stampDutyPaid = stampDuty, subOffice = subOffice,
                createdAt = Instant.now(), updatedAt = Instant.now()
            )
            relationalDao.insertRegistrations(listOf(newReg))
            SyncManager.enqueueGranularSync(context, "registration", tempId.toString(), "insert")
            _uiEvent.emit("Registration added and queued")
        }
    }

    fun submitOwnershipMutation(ulpin: String, name: String, khata: String, rightType: String, share: Double, email: String? = null) {
        viewModelScope.launch {
            var userId: String? = null
            if (!email.isNullOrBlank()) {
                try {
                    val response = supabase.postgrest.rpc(
                        "get_user_id_by_email",
                        mapOf("p_email" to email)
                    )
                    if (response.data.isNotBlank() && response.data != "null") {
                        userId = response.data.replace("\"", "") // Remove quotes from json string
                    }
                } catch (e: Exception) {
                    _uiEvent.emit("Could not find user with email $email")
                    return@launch
                }
            }

            val newOwner = com.govtech.landstack.data.remote.RemoteParcelOwner(
                ulpin = ulpin,
                ownerName = name,
                khataNumber = khata,
                rightType = rightType,
                ownershipShare = share,
                userId = userId
            )
            val result = syncRepository.executeOwnershipMutation(ulpin, listOf(newOwner))
            if (result.isSuccess) {
                // Refresh data from remote
                syncRepository.pullOwners()
                _mutationLogs.value = syncRepository.fetchOwnershipMutations(ulpin)
                _uiEvent.emit("Mutation successful")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                _uiEvent.emit("Mutation failed: $errorMsg")
            }
        }
    }

    fun recordOwnershipMutation(context: Context, ulpin: String, name: String, khata: String, rightType: String, share: Double) {
        viewModelScope.launch {
            val currentOwners = _owners.value
            val activeOwners = currentOwners.filter { it.effectiveTo == null }
            val now = Instant.now()
            
            // Close out active owners locally
            val closedOwners = activeOwners.map { it.copy(effectiveTo = now) }
            if (closedOwners.isNotEmpty()) {
                relationalDao.insertOwners(closedOwners)
            }
            
            val tempId = -System.currentTimeMillis()
            val newOwner = OwnerEntity(
                id = tempId, ulpin = ulpin, ownerName = name, userId = null,
                khataNumber = khata, rightType = rightType, ownershipShare = share,
                effectiveFrom = now, effectiveTo = null,
                createdAt = now, updatedAt = now
            )
            relationalDao.insertOwners(listOf(newOwner))
            SyncManager.enqueueGranularSync(context, "owner", tempId.toString(), "insert")
            _uiEvent.emit("Ownership mutation recorded and queued")
        }
    }

    private fun emptyParcel() = Parcel(
        base = BaseLayer(
            ulpin = "",
            state = "",
            district = "",
            villageWard = "",
            surveyNumber = "",
            parcelType = "",
            areaSqm = 0.0,
            geometry = Geometry(type = "Polygon", coordinates = emptyList())
        ),
        essential = EssentialLayer(
            ror = RecordOfRights(emptyList(), "", "", 0.0, emptyList()),
            registration = Registration("", "", "", 0.0, ""),
            zoning = Zoning("", 0.0, ""),
            building = Building("", 0.0, 0, ""),
            encumbrance = Encumbrance("", 0.0, "", "")
        ),
        additional = AdditionalLayer(
            utilities = Utilities("", "", false, ""),
            taxation = Taxation(0.0, 0.0, "", 0.0),
            valuation = Valuation(0.0, 0.0),
            environmental = Environmental(false, false, false)
        )
    )
    fun resolveConflict(keepMine: Boolean) {
        val conflict = _pendingConflict.value ?: return
        viewModelScope.launch {
            if (keepMine) {
                // 1. Force push bypassing the conflict check by passing server's latest updated_at
                val localParcel = parcelDao.getParcelSync(conflict.ulpin)
                if (localParcel != null) {
                    try {
                        val remoteParcels = supabase.postgrest["parcels"]
                            .select { filter { eq("ulpin", conflict.ulpin) } }
                            .decodeList<com.govtech.landstack.data.remote.RemoteParcel>()
                        val serverUpdatedAt = remoteParcels.firstOrNull()?.updatedAt ?: conflict.baseUpdatedAt

                        val args = kotlinx.serialization.json.buildJsonObject {
                            put("p_ulpin", kotlinx.serialization.json.JsonPrimitive(conflict.ulpin))
                            put("p_base_updated_at", kotlinx.serialization.json.JsonPrimitive(serverUpdatedAt))
                            put("p_state", kotlinx.serialization.json.JsonPrimitive(localParcel.state))
                            put("p_district", kotlinx.serialization.json.JsonPrimitive(localParcel.district))
                        
                        val parcelData = try { kotlinx.serialization.json.Json.parseToJsonElement(localParcel.parcelDataJson) } catch(e:Exception) { null }
                        if (parcelData != null) {
                            put("p_parcel_data", parcelData)
                        } else {
                            put("p_parcel_data", kotlinx.serialization.json.JsonNull)
                        }
                    }
                        supabase.postgrest.rpc("push_parcel_with_conflict_check", args)
                        
                        // Audit Log for forced push
                        val user = supabase.auth.currentUserOrNull()
                        if (user != null) {
                            val actionDesc = "conflict_resolved_kept_local"
                            val log = AuditLogEntity(
                                timestamp = Instant.now().toEpochMilli(),
                                userRole = _userRole.value ?: "Citizen",
                                ulpin = conflict.ulpin,
                                action = actionDesc
                            )
                            auditLogDao.insertLog(log)
                        }
                        
                        // Clear the conflict
                        pendingConflictDao.deleteConflict(conflict.ulpin)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // Keep Theirs
                // Just pull the parcel from remote and replace local
                try {
                    val remoteParcels = supabase.postgrest["parcels"]
                        .select { filter { eq("ulpin", conflict.ulpin) } }
                        .decodeList<com.govtech.landstack.data.remote.RemoteParcel>()
                    
                    val remote = remoteParcels.firstOrNull()
                    if (remote != null) {
                        val localEntity = ParcelEntity(
                            ulpin = remote.ulpin,
                            state = remote.state,
                            district = remote.district,
                            zoningClassification = remote.zoningClassification ?: "Unknown",
                            permittedUse = remote.permittedUse ?: "Unknown",
                            parcelDataJson = remote.parcelData?.toString() ?: "{}",
                            updatedAt = remote.updatedAt ?: "1970-01-01T00:00:00Z"
                        )
                        parcelDao.insertParcels(listOf(localEntity))
                        // Audit Log for keep theirs
                        val user = supabase.auth.currentUserOrNull()
                        if (user != null) {
                            val actionDesc = "conflict_resolved_kept_remote"
                            val log = AuditLogEntity(
                                timestamp = Instant.now().toEpochMilli(),
                                userRole = _userRole.value ?: "Unknown",
                                ulpin = conflict.ulpin,
                                action = actionDesc
                            )
                            auditLogDao.insertLog(log)
                        }

                    }
                } catch (e: Exception) {
                    _uiEvent.emit("Error fetching remote parcel: ${e.message}")
                }
                // Delete conflict
                pendingConflictDao.deleteConflict(conflict.ulpin)
            }
        }
    }
    
    fun verifyDocumentHeuristic(document: DocumentEntity): String {
        val ocr = document.ocrExtractedText ?: return "N/A"
        
        val currentState = _parcelState.value
        val parcel = if (currentState is com.govtech.landstack.ui.util.UiState.Success) currentState.data else return "N/A"
        val owner = _owners.value.firstOrNull()
        val ownerName = owner?.ownerName ?: ""
        val surveyNumber = parcel.base.surveyNumber
        
        var nameMatch = false
        var surveyMatch = false
        
        if (ownerName.isNotEmpty() && ocr.contains(ownerName, ignoreCase = true)) {
            nameMatch = true
        }
        
        if (surveyNumber.isNotEmpty() && ocr.contains(surveyNumber, ignoreCase = true)) {
            surveyMatch = true
        }
        
        return if (nameMatch || surveyMatch) {
            "✓ No mismatch detected"
        } else {
            "⚠ Possible mismatch — review manually"
        }
    }
}
