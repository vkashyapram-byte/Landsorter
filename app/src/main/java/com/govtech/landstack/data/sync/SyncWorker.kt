package com.govtech.landstack.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.data.remote.RemoteBuildingPermission
import com.govtech.landstack.data.remote.RemoteParcelOwner
import com.govtech.landstack.data.remote.RemoteParcelEncumbrance
import com.govtech.landstack.data.remote.RemoteParcelTaxRecord
import com.govtech.landstack.data.remote.RemoteParcelRegistration
import com.govtech.landstack.data.repository.SyncRepository
import com.govtech.landstack.data.repository.SyncResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val database: LandStackDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val entityType = inputData.getString("entity_type")
        val entityId = inputData.getString("entity_id")
        val actionType = inputData.getString("action_type")

        if (entityType != null && entityId != null && actionType != null) {
            val result = executeGranularPush(entityType, entityId, actionType)
            return@coroutineScope when (result) {
                is SyncResult.Success -> Result.success()
                is SyncResult.PermissionDenied -> Result.failure() // Drop it, don't retry endlessly
                is SyncResult.Conflict -> {
                    if (entityType == "parcel") {
                        val localParcel = database.parcelDao().getParcelSync(entityId)
                        if (localParcel != null) {
                            database.pendingConflictDao().insertConflict(
                                com.govtech.landstack.data.local.PendingConflictEntity(
                                    ulpin = localParcel.ulpin,
                                    localDataJson = localParcel.parcelDataJson,
                                    baseUpdatedAt = localParcel.updatedAt,
                                    message = result.message
                                )
                            )
                        }
                    }
                    Result.failure() // Don't retry automatically
                }
                is SyncResult.NetworkError -> Result.retry()
                else -> Result.failure()
            }
        }

        // Global Sync
        val pushParcelsResult = syncRepository.pushParcelsToRemote()
        val pushLogsResult = syncRepository.pushAuditLogsToRemote()
        
        val pulls = listOf(
            syncRepository.pullParcelsFromRemote(),
            syncRepository.pullOwners(),
            syncRepository.pullPermissions(),
            syncRepository.pullEncumbrances(),
            syncRepository.pullTaxRecords(),
            syncRepository.pullRegistrations(),
            syncRepository.pullDocuments(),
            syncRepository.pullDisputes(),
            syncRepository.pullDataConflicts(),
            syncRepository.pullApplications(),
            syncRepository.pullPropertyTransactions(),
            syncRepository.pullServiceRequests()
        )

        val results = pulls + pushParcelsResult + pushLogsResult

        if (results.any { it is SyncResult.NetworkError }) {
            return@coroutineScope Result.retry()
        }

        Result.success()
    }

    private suspend fun executeGranularPush(entityType: String, entityId: String, actionType: String): SyncResult {
        return when (entityType) {
            "parcel" -> {
                syncRepository.pushSingleParcel(entityId)
            }
            "owner" -> {
                val owner = database.relationalDao().getOwnerSync(entityId.toLong()) ?: return SyncResult.UnknownError("Not found")
                when (actionType) {
                    "insert" -> {
                        val res = syncRepository.pushOwnershipMutation(
                            owner.ulpin,
                            listOf(
                                RemoteParcelOwner(
                                    ulpin = owner.ulpin,
                                    ownerName = owner.ownerName,
                                    userId = owner.userId,
                                    khataNumber = owner.khataNumber,
                                    rightType = owner.rightType,
                                    ownershipShare = owner.ownershipShare
                                )
                            )
                        )
                        if (res is SyncResult.Success) {
                            database.relationalDao().deleteOwnerSync(owner.id)
                            syncRepository.pullOwners()
                        }
                        res
                    }
                    else -> SyncResult.UnknownError("Unknown action")
                }
            }
            "permit" -> {
                val permit = database.relationalDao().getPermissionSync(entityId.toLong()) ?: return SyncResult.UnknownError("Not found")
                when (actionType) {
                    "insert" -> {
                        val res = syncRepository.pushNewPermission(
                            RemoteBuildingPermission(
                                ulpin = permit.ulpin,
                                sanctionNumber = permit.sanctionNumber,
                                approvedBuiltUpArea = permit.approvedBuiltUpArea,
                                floors = permit.floors,
                                status = permit.status,
                                submittedBy = permit.submittedBy,
                                decidedBy = permit.decidedBy,
                                decidedAt = permit.decidedAt?.toString()
                            )
                        )
                        if (res is SyncResult.Success) {
                            database.relationalDao().deletePermissionSync(permit.id)
                            syncRepository.pullPermissions()
                        }
                        res
                    }
                    "decision" -> syncRepository.pushPermissionStatusChange(permit.id, permit.status)
                    else -> SyncResult.UnknownError("Unknown action")
                }
            }
            "encumbrance" -> {
                val enc = database.relationalDao().getEncumbranceSync(entityId.toLong()) ?: return SyncResult.UnknownError("Not found")
                when (actionType) {
                    "insert" -> {
                        val res = syncRepository.pushNewEncumbrance(
                            RemoteParcelEncumbrance(
                                ulpin = enc.ulpin,
                                lender = enc.lender,
                                loanAmount = enc.loanAmount,
                                lienStatus = enc.lienStatus,
                                validFrom = enc.validFrom?.toString(),
                                validTo = enc.validTo?.toString(),
                                resolvedAt = enc.resolvedAt?.toString()
                            )
                        )
                        if (res is SyncResult.Success) {
                            database.relationalDao().deleteEncumbranceSync(enc.id)
                            syncRepository.pullEncumbrances()
                        }
                        res
                    }
                    "resolve" -> syncRepository.pushResolveEncumbrance(enc.id)
                    else -> SyncResult.UnknownError("Unknown action")
                }
            }
            "tax_record" -> {
                val tax = database.relationalDao().getTaxRecordSync(entityId.toLong()) ?: return SyncResult.UnknownError("Not found")
                when (actionType) {
                    "insert" -> {
                        val res = syncRepository.pushNewTaxRecord(
                            RemoteParcelTaxRecord(
                                ulpin = tax.ulpin,
                                taxYear = tax.taxYear,
                                assessedValue = tax.assessedValue,
                                annualTax = tax.annualTax,
                                paymentStatus = tax.paymentStatus,
                                arrears = tax.arrears,
                                paidAt = tax.paidAt?.toString()
                            )
                        )
                        if (res is SyncResult.Success) {
                            database.relationalDao().deleteTaxRecordSync(tax.id)
                            syncRepository.pullTaxRecords()
                        }
                        res
                    }
                    "mark_paid" -> syncRepository.pushMarkTaxPaid(tax.id)
                    else -> SyncResult.UnknownError("Unknown action")
                }
            }
            "registration" -> {
                val reg = database.relationalDao().getRegistrationSync(entityId.toLong()) ?: return SyncResult.UnknownError("Not found")
                when (actionType) {
                    "insert" -> {
                        val res = syncRepository.pushNewRegistration(
                            RemoteParcelRegistration(
                                ulpin = reg.ulpin,
                                deedNumber = reg.deedNumber,
                                registrationDate = reg.registrationDate,
                                transactionType = reg.transactionType,
                                stampDutyPaid = reg.stampDutyPaid,
                                subOffice = reg.subOffice
                            )
                        )
                        if (res is SyncResult.Success) {
                            database.relationalDao().deleteRegistrationSync(reg.id)
                            syncRepository.pullRegistrations()
                        }
                        res
                    }
                    else -> SyncResult.UnknownError("Unknown action")
                }
            }
            else -> SyncResult.UnknownError("Unknown entity type")
        }
    }
}
