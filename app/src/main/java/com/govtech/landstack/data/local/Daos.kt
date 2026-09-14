package com.govtech.landstack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels")
    fun getAllParcels(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE ulpin = :ulpin")
    fun getParcel(ulpin: String): Flow<ParcelEntity?>

    @Query("SELECT * FROM parcels WHERE ulpin = :ulpin")
    suspend fun getParcelSync(ulpin: String): ParcelEntity?

    @Query("SELECT zoningClassification, COUNT(*) as count FROM parcels GROUP BY zoningClassification")
    fun getZoningCounts(): Flow<List<ZoningCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcels(parcels: List<ParcelEntity>)

    @Query("SELECT * FROM parcels")
    suspend fun getAllParcelsSync(): List<ParcelEntity>
    
    @Query("SELECT COUNT(*) FROM parcels")
    fun getTotalParcelsCount(): Flow<Int>
}

data class ZoningCount(
    val zoningClassification: String,
    val count: Int
)

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    suspend fun getAllLogsSync(): List<AuditLogEntity>
}

@Dao
interface RelationalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwners(owners: List<OwnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissions(permissions: List<BuildingPermissionEntity>)

    @Query("SELECT * FROM building_permissions WHERE status = 'applied'")
    fun getPendingPermissions(): Flow<List<BuildingPermissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncumbrances(encumbrances: List<EncumbranceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxRecords(taxRecords: List<TaxRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistrations(registrations: List<RegistrationEntity>)

    @Query("SELECT * FROM parcel_owners WHERE id = :id")
    suspend fun getOwnerSync(id: Long): OwnerEntity?

    @Query("SELECT * FROM building_permissions WHERE id = :id")
    suspend fun getPermissionSync(id: Long): BuildingPermissionEntity?

    @Query("SELECT * FROM parcel_encumbrances WHERE id = :id")
    suspend fun getEncumbranceSync(id: Long): EncumbranceEntity?

    @Query("SELECT * FROM parcel_tax_records WHERE id = :id")
    suspend fun getTaxRecordSync(id: Long): TaxRecordEntity?

    @Query("SELECT * FROM parcel_registrations WHERE id = :id")
    suspend fun getRegistrationSync(id: Long): RegistrationEntity?

    @Query("DELETE FROM parcel_owners WHERE id = :id")
    suspend fun deleteOwnerSync(id: Long)

    @Query("DELETE FROM building_permissions WHERE id = :id")
    suspend fun deletePermissionSync(id: Long)

    @Query("DELETE FROM parcel_encumbrances WHERE id = :id")
    suspend fun deleteEncumbranceSync(id: Long)

    @Query("DELETE FROM parcel_tax_records WHERE id = :id")
    suspend fun deleteTaxRecordSync(id: Long)

    @Query("DELETE FROM parcel_registrations WHERE id = :id")
    suspend fun deleteRegistrationSync(id: Long)

    @Query("SELECT * FROM parcel_owners WHERE ulpin = :ulpin ORDER BY effectiveFrom DESC")
    fun getOwnersForParcel(ulpin: String): Flow<List<OwnerEntity>>

    @Query("SELECT * FROM building_permissions WHERE ulpin = :ulpin ORDER BY createdAt DESC")
    fun getPermissionsForParcel(ulpin: String): Flow<List<BuildingPermissionEntity>>

    @Query("SELECT * FROM parcel_encumbrances WHERE ulpin = :ulpin ORDER BY createdAt DESC")
    fun getEncumbrancesForParcel(ulpin: String): Flow<List<EncumbranceEntity>>

    @Query("SELECT * FROM parcel_tax_records WHERE ulpin = :ulpin ORDER BY taxYear DESC")
    fun getTaxRecordsForParcel(ulpin: String): Flow<List<TaxRecordEntity>>

    @Query("SELECT * FROM parcel_registrations WHERE ulpin = :ulpin ORDER BY createdAt DESC")
    fun getRegistrationsForParcel(ulpin: String): Flow<List<RegistrationEntity>>
    
    @Query("SELECT COUNT(*) FROM parcel_encumbrances WHERE lienStatus = 'active'")
    fun getActiveEncumbrancesCount(): Flow<Int>
    
    @Query("SELECT COUNT(DISTINCT ownerName) FROM parcel_owners")
    fun getTotalOwnersCount(): Flow<Int>
}

@Dao
interface PendingConflictDao {
    @Query("SELECT * FROM pending_conflicts WHERE ulpin = :ulpin")
    fun getConflict(ulpin: String): Flow<PendingConflictEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: PendingConflictEntity)

    @Query("DELETE FROM pending_conflicts WHERE ulpin = :ulpin")
    suspend fun deleteConflict(ulpin: String)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE ulpin = :ulpin")
    fun getDocumentsByUlpin(ulpin: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes WHERE ulpin = :ulpin")
    fun getDisputesByUlpin(ulpin: String): Flow<List<DisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisputes(disputes: List<DisputeEntity>)
}

@Dao
interface RestrictionDao {
    @Query("SELECT * FROM parcel_restrictions WHERE ulpin = :ulpin")
    fun getRestrictionsByUlpin(ulpin: String): Flow<List<RestrictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestrictions(restrictions: List<RestrictionEntity>)
}

@Dao
interface DataConflictDao {
    @Query("SELECT * FROM data_conflicts WHERE ulpin = :ulpin ORDER BY createdAt DESC")
    fun getConflictsForParcel(ulpin: String): Flow<List<DataConflictEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflicts(conflicts: List<DataConflictEntity>)

    @Query("SELECT * FROM data_conflicts ORDER BY createdAt DESC")
    fun getAllConflicts(): Flow<List<DataConflictEntity>>
    
    @Query("SELECT COUNT(*) FROM data_conflicts WHERE status = 'open'")
    fun getOpenConflictsCount(): Flow<Int>

    @Query("DELETE FROM data_conflicts")
    suspend fun clear()
}

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications WHERE ulpin = :ulpin ORDER BY createdAt DESC")
    fun getApplicationsForParcel(ulpin: String): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications ORDER BY createdAt DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Query("SELECT COUNT(*) FROM applications WHERE status != 'completed'")
    fun getActiveApplicationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(apps: List<ApplicationEntity>)

    @Query("DELETE FROM applications")
    suspend fun clear()
}

@Dao
interface ApplicationStageDao {
    @Query("SELECT * FROM application_stages WHERE applicationId = :appId ORDER BY timestamp ASC")
    fun getStagesForApplication(appId: Long): Flow<List<ApplicationStageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<ApplicationStageEntity>)

    @Query("DELETE FROM application_stages")
    suspend fun clear()
}

@Dao
interface PropertyTransactionDao {
    @Query("SELECT * FROM property_transactions WHERE ulpin = :ulpin ORDER BY created_at DESC")
    fun getTransactionsForParcel(ulpin: String): Flow<List<PropertyTransactionEntity>>

    @Query("SELECT * FROM property_transactions WHERE buyer_user_id = :userId ORDER BY created_at DESC")
    fun getTransactionsForUser(userId: String): Flow<List<PropertyTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<PropertyTransactionEntity>)

    @Query("DELETE FROM property_transactions")
    suspend fun clear()
}

@Dao
interface ServiceRequestDao {
    @Query("SELECT * FROM service_requests WHERE citizen_user_id = :userId ORDER BY created_at DESC")
    fun getServiceRequestsForUser(userId: String): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests ORDER BY created_at DESC")
    fun getAllServiceRequests(): Flow<List<ServiceRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRequests(requests: List<ServiceRequestEntity>)

    @Query("DELETE FROM service_requests")
    suspend fun clear()
}
