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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcels(parcels: List<ParcelEntity>)

    @Query("SELECT * FROM parcels")
    suspend fun getAllParcelsSync(): List<ParcelEntity>
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    suspend fun getAllLogsSync(): List<AuditLogEntity>
}
