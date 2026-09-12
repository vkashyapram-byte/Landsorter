package com.govtech.landstack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ParcelEntity::class, AuditLogEntity::class], version = 1, exportSchema = false)
abstract class LandStackDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
    abstract fun auditLogDao(): AuditLogDao
}
