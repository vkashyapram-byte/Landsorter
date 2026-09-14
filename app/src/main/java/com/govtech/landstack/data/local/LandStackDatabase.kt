package com.govtech.landstack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

    @Database(
    entities = [
        ParcelEntity::class, 
        AuditLogEntity::class,
        OwnerEntity::class,
        BuildingPermissionEntity::class,
        EncumbranceEntity::class,
        TaxRecordEntity::class,
        RegistrationEntity::class,
        PendingConflictEntity::class,
        DocumentEntity::class,
        DisputeEntity::class,
        RestrictionEntity::class,
        DataConflictEntity::class,
        ApplicationEntity::class,
        ApplicationStageEntity::class,
        PropertyTransactionEntity::class,
        ServiceRequestEntity::class
    ], 
    version = 8, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LandStackDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun relationalDao(): RelationalDao
    abstract fun pendingConflictDao(): PendingConflictDao
    abstract fun documentDao(): DocumentDao
    abstract fun disputeDao(): DisputeDao
    abstract fun restrictionDao(): RestrictionDao
    abstract fun dataConflictDao(): DataConflictDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun applicationStageDao(): ApplicationStageDao
    abstract fun propertyTransactionDao(): PropertyTransactionDao
    abstract fun serviceRequestDao(): ServiceRequestDao
}
