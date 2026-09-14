package com.govtech.landstack.data.repository

import android.content.Context
import com.govtech.landstack.data.local.AuditLogEntity
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.data.local.ParcelEntity
import com.govtech.landstack.data.local.*
import com.govtech.landstack.data.remote.*
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.Json
import java.time.Instant

@Serializable
data class RemoteAuditLog(
    val timestamp: Long,
    val user_role: String,
    val ulpin: String,
    val action: String,
    val user_id: String? = null,
    val old_data: JsonElement? = null,
    val new_data: JsonElement? = null
)

@Singleton
class SyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: LandStackDatabase,
    private val supabase: SupabaseClient
) {
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    private fun parseError(e: Exception): SyncResult {
        val msg = e.message ?: ""
        println("parseError caught: ${e.message}"); return if (msg.contains("row-level security") || msg.contains("403") || msg.contains("400") || msg.contains("Permission denied")) {
            SyncResult.PermissionDenied
        } else {
            SyncResult.NetworkError()
        }
    }

    // -------------------------------------------------------------------------
    // Parcels
    // -------------------------------------------------------------------------

    suspend fun pushSingleParcel(ulpin: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            val localParcel = database.parcelDao().getParcelSync(ulpin) ?: return@withContext SyncResult.UnknownError("Parcel not found locally")
            
            val parcelData = try { Json.parseToJsonElement(localParcel.parcelDataJson) } catch(e:Exception) { null }
            val args = kotlinx.serialization.json.buildJsonObject {
                put("p_ulpin", kotlinx.serialization.json.JsonPrimitive(localParcel.ulpin))
                put("p_base_updated_at", kotlinx.serialization.json.JsonPrimitive(localParcel.updatedAt))
                put("p_state", kotlinx.serialization.json.JsonPrimitive(localParcel.state))
                put("p_district", kotlinx.serialization.json.JsonPrimitive(localParcel.district))
                if (parcelData != null) {
                    put("p_parcel_data", parcelData)
                } else {
                    put("p_parcel_data", kotlinx.serialization.json.JsonNull)
                }
            }
            
            supabase.postgrest.rpc("push_parcel_with_conflict_check", args)
            SyncResult.Success
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("CONFLICT_DETECTED")) {
                return@withContext SyncResult.Conflict(ulpin, msg)
            }
            val res = parseError(e)
            if (res is SyncResult.NetworkError) {
                com.govtech.landstack.data.sync.SyncManager.enqueueSync(context)
            }
            res
        }
    }

    suspend fun checkParcelExistsRemotely(ulpin: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["parcels"]
                .select { filter { eq("ulpin", ulpin) } }
                .decodeList<RemoteParcel>()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun pushParcelsToRemote(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val localParcels = database.parcelDao().getAllParcelsSync()
            val remoteParcels = localParcels.map {
                RemoteParcel(
                    ulpin = it.ulpin,
                    state = it.state,
                    district = it.district,
                    villageWard = null,
                    surveyNumber = null,
                    parcelType = null,
                    areaSqm = null,
                    zoningClassification = null,
                    permissibleFsi = null,
                    permittedUse = null,
                    parcelData = try { Json.parseToJsonElement(it.parcelDataJson) } catch(e:Exception) { null }
                )
            }
            if (remoteParcels.isNotEmpty()) {
                supabase.postgrest["parcels"].upsert(remoteParcels)
            }
            SyncResult.Success
        } catch (e: Exception) {
            parseError(e)
        }
    }
    
    suspend fun pullParcelsFromRemote(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_at_parcels", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remoteParcels = supabase.postgrest["parcels"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteParcel>()

            val localEntities = remoteParcels.map {
                ParcelEntity(
                    ulpin = it.ulpin,
                    state = it.state,
                    district = it.district,
                    zoningClassification = it.zoningClassification ?: "Unknown",
                    permittedUse = it.permittedUse ?: "Unknown",
                    parcelDataJson = it.parcelData?.toString() ?: "{}",
                    updatedAt = it.updatedAt ?: "1970-01-01T00:00:00Z"
                )
            }
            
            if (localEntities.isNotEmpty()) {
                database.parcelDao().insertParcels(localEntities)
            }
            
            prefs.edit().putString("last_synced_at_parcels", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) {
            parseError(e)
        }
    }

    // -------------------------------------------------------------------------
    // Audit Logs
    // -------------------------------------------------------------------------

    suspend fun pushAuditLogsToRemote(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
            val localLogs = database.auditLogDao().getAllLogsSync()
            val remoteLogs = localLogs.map {
                RemoteAuditLog(
                    timestamp = it.timestamp,
                    user_role = it.userRole,
                    ulpin = it.ulpin,
                    action = it.action,
                    user_id = currentUserId
                )
            }
            
            if (remoteLogs.isNotEmpty()) {
                supabase.postgrest["audit_logs"].insert(remoteLogs)
            }
            SyncResult.Success
        } catch (e: Exception) {
            parseError(e)
        }
    }

    // -------------------------------------------------------------------------
    // Delta Pull Functions (New Tables)
    // -------------------------------------------------------------------------

    suspend fun pullOwners(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_owners", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["parcel_owners"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteParcelOwner>()

            val locals = remotes.map {
                OwnerEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    ownerName = it.ownerName,
                    userId = it.userId,
                    khataNumber = it.khataNumber,
                    rightType = it.rightType,
                    ownershipShare = it.ownershipShare,
                    effectiveFrom = Instant.parse(it.effectiveFrom ?: "1970-01-01T00:00:00Z"),
                    effectiveTo = it.effectiveTo?.let { Instant.parse(it) },
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.relationalDao().insertOwners(locals)
            prefs.edit().putString("last_synced_owners", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullPermissions(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_permissions", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["building_permissions"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteBuildingPermission>()

            val locals = remotes.map {
                BuildingPermissionEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    sanctionNumber = it.sanctionNumber,
                    approvedBuiltUpArea = it.approvedBuiltUpArea,
                    floors = it.floors,
                    status = it.status ?: "applied",
                    submittedBy = it.submittedBy,
                    decidedBy = it.decidedBy,
                    decidedAt = it.decidedAt?.let { t -> Instant.parse(t) },
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.relationalDao().insertPermissions(locals)
            prefs.edit().putString("last_synced_permissions", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullEncumbrances(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_encumbrances", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["parcel_encumbrances"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteParcelEncumbrance>()

            val locals = remotes.map {
                EncumbranceEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    lender = it.lender,
                    loanAmount = it.loanAmount,
                    lienStatus = it.lienStatus,
                    validFrom = it.validFrom?.let { t -> Instant.parse(t) },
                    validTo = it.validTo?.let { t -> Instant.parse(t) },
                    resolvedAt = it.resolvedAt?.let { t -> Instant.parse(t) },
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.relationalDao().insertEncumbrances(locals)
            prefs.edit().putString("last_synced_encumbrances", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullTaxRecords(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_tax_records", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["parcel_tax_records"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteParcelTaxRecord>()

            val locals = remotes.map {
                TaxRecordEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    taxYear = it.taxYear,
                    assessedValue = it.assessedValue,
                    annualTax = it.annualTax,
                    paymentStatus = it.paymentStatus,
                    arrears = it.arrears,
                    paidAt = it.paidAt?.let { t -> Instant.parse(t) },
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.relationalDao().insertTaxRecords(locals)
            prefs.edit().putString("last_synced_tax_records", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullRegistrations(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_registrations", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["parcel_registrations"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteParcelRegistration>()

            val locals = remotes.map {
                RegistrationEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    deedNumber = it.deedNumber,
                    registrationDate = it.registrationDate,
                    transactionType = it.transactionType,
                    stampDutyPaid = it.stampDutyPaid,
                    subOffice = it.subOffice,
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.relationalDao().insertRegistrations(locals)
            prefs.edit().putString("last_synced_registrations", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullDocuments(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_documents", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["documents"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteDocument>()

            val locals = remotes.map {
                DocumentEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    docType = it.docType,
                    filePath = it.filePath,
                    uploadedBy = it.uploadedBy,
                    verificationStatus = it.verificationStatus,
                    ocrExtractedText = it.ocrExtractedText,
                    ocrProcessedAt = it.ocrProcessedAt?.let { t -> Instant.parse(t) },
                    verifiedBy = it.verifiedBy,
                    verifiedAt = it.verifiedAt?.let { t -> Instant.parse(t) },
                    registrationId = it.registrationId,
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.documentDao().insertDocuments(locals)
            prefs.edit().putString("last_synced_documents", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullDisputes(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_disputes", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["disputes"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteDispute>()

            val locals = remotes.map {
                DisputeEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    caseNumber = it.caseNumber,
                    disputeType = it.disputeType,
                    description = it.description,
                    source = it.source,
                    sourceReference = it.sourceReference,
                    effectiveDate = it.effectiveDate,
                    isStayOrder = it.isStayOrder,
                    status = it.status,
                    partiesInvolved = it.partiesInvolved,
                    filedBy = it.filedBy,
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.disputeDao().insertDisputes(locals)
            prefs.edit().putString("last_synced_disputes", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullRestrictions(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_restrictions", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["parcel_restrictions"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteRestriction>()

            val locals = remotes.map {
                RestrictionEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    restrictionType = it.restrictionType,
                    description = it.description,
                    source = it.source,
                    effectiveFrom = it.effectiveFrom,
                    effectiveTo = it.effectiveTo,
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.restrictionDao().insertRestrictions(locals)
            prefs.edit().putString("last_synced_restrictions", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullDataConflicts(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_data_conflicts", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["data_conflicts"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteDataConflict>()

            val locals = remotes.map {
                DataConflictEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    category = it.category,
                    severity = it.severity,
                    status = it.status,
                    details = it.details,
                    mismatchedValues = it.mismatchedValues?.toString(),
                    assignedTo = it.assignedTo,
                    resolvedAt = it.resolvedAt?.let { t -> Instant.parse(t) },
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.dataConflictDao().insertConflicts(locals)
            prefs.edit().putString("last_synced_data_conflicts", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullApplications(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_applications", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["applications"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteApplication>()

            val locals = remotes.map {
                ApplicationEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    applicantId = it.applicantId,
                    applicationType = it.applicationType,
                    status = it.status,
                    currentStage = it.currentStage,
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.applicationDao().insertApplications(locals)
            prefs.edit().putString("last_synced_applications", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullPropertyTransactions(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_property_transactions", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["property_transactions"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemotePropertyTransaction>()

            val locals = remotes.map {
                PropertyTransactionEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    buyerName = it.buyerName,
                    buyerUserId = it.buyerUserId,
                    sellerOwnerId = it.sellerOwnerId,
                    status = it.status,
                    initiatedBy = it.initiatedBy,
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.propertyTransactionDao().insertTransactions(locals)
            prefs.edit().putString("last_synced_property_transactions", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pullServiceRequests(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = prefs.getString("last_synced_service_requests", "1970-01-01T00:00:00Z") ?: "1970-01-01T00:00:00Z"
            val remotes = supabase.postgrest["service_requests"]
                .select { filter { gt("updated_at", lastSync) } }
                .decodeList<RemoteServiceRequest>()

            val locals = remotes.map {
                ServiceRequestEntity(
                    id = it.id ?: 0,
                    ulpin = it.ulpin,
                    citizenUserId = it.citizenUserId,
                    requestType = it.requestType,
                    description = it.description,
                    status = it.status,
                    handledBy = it.handledBy,
                    handledAt = it.handledAt?.let { t -> Instant.parse(t) },
                    createdAt = Instant.parse(it.createdAt ?: "1970-01-01T00:00:00Z"),
                    updatedAt = Instant.parse(it.updatedAt ?: "1970-01-01T00:00:00Z")
                )
            }
            if (locals.isNotEmpty()) database.serviceRequestDao().insertServiceRequests(locals)
            prefs.edit().putString("last_synced_service_requests", Instant.now().toString()).apply()
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    // -------------------------------------------------------------------------
    // Scoped Push Functions
    // -------------------------------------------------------------------------

    suspend fun pushNewPermission(permit: RemoteBuildingPermission): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["building_permissions"].insert(permit)
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushPermissionStatusChange(id: Long, decision: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc("approve_building_permit", RpcApproveBuildingPermitRequest(id, decision))
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushOwnershipMutation(ulpin: String, owners: List<RemoteParcelOwner>): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc("record_ownership_mutation", RpcOwnershipMutationRequest(ulpin, owners))
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun executeOwnershipMutation(ulpin: String, owners: List<RemoteParcelOwner>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc("record_ownership_mutation", RpcOwnershipMutationRequest(ulpin, owners))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchOwnershipMutations(ulpin: String): List<RemoteAuditLog> = withContext(Dispatchers.IO) {
        try {
            val logs = supabase.postgrest["audit_logs"]
                .select {
                    filter {
                        eq("ulpin", ulpin)
                    }
                }
                .decodeList<RemoteAuditLog>()
            logs.filter { it.action in listOf("owner_added", "owner_updated", "owner_closed") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun pushNewEncumbrance(encumbrance: RemoteParcelEncumbrance): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["parcel_encumbrances"].insert(encumbrance)
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushResolveEncumbrance(id: Long): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc("resolve_parcel_encumbrance", RpcResolveEncumbranceRequest(id))
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushNewTaxRecord(tax: RemoteParcelTaxRecord): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["parcel_tax_records"].insert(tax)
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushMarkTaxPaid(id: Long): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc("mark_tax_record_paid", RpcMarkTaxPaidRequest(id))
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushNewRegistration(reg: RemoteParcelRegistration): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["parcel_registrations"].insert(reg)
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushNewPropertyTransaction(transaction: RemotePropertyTransaction): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["property_transactions"].insert(transaction)
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushAdvancePropertyTransactionStatus(id: Long, newStatus: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc("advance_property_transaction_status", RpcAdvancePropertyTransactionStatusRequest(id, newStatus))
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushNewServiceRequest(request: RemoteServiceRequest): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["service_requests"].insert(request)
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }

    suspend fun pushUpdateServiceRequestStatus(id: Long, newStatus: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc("update_service_request_status", RpcUpdateServiceRequestStatusRequest(id, newStatus))
            SyncResult.Success
        } catch (e: Exception) { parseError(e) }
    }
}
