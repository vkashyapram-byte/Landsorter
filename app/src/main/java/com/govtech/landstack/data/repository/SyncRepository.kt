package com.govtech.landstack.data.repository

import com.govtech.landstack.data.local.AuditLogEntity
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.data.local.ParcelEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.Json

@Serializable
data class RemoteParcel(
    val ulpin: String,
    val state: String,
    val district: String,
    val parcel_data: JsonElement
)

@Serializable
data class RemoteAuditLog(
    val timestamp: Long,
    val user_role: String,
    val ulpin: String,
    val action: String
)

@Singleton
class SyncRepository @Inject constructor(
    private val database: LandStackDatabase,
    private val supabase: SupabaseClient
) {
    suspend fun pushParcelsToRemote() = withContext(Dispatchers.IO) {
        val localParcels = database.parcelDao().getAllParcelsSync()
        val remoteParcels = localParcels.map {
            RemoteParcel(
                ulpin = it.ulpin,
                state = it.state,
                district = it.district,
                parcel_data = Json.parseToJsonElement(it.parcelDataJson)
            )
        }
        
        if (remoteParcels.isNotEmpty()) {
            supabase.postgrest["parcels"].upsert(remoteParcels)
        }
    }
    
    suspend fun pushAuditLogsToRemote() = withContext(Dispatchers.IO) {
        val localLogs = database.auditLogDao().getAllLogsSync()
        val remoteLogs = localLogs.map {
            RemoteAuditLog(
                timestamp = it.timestamp,
                user_role = it.userRole,
                ulpin = it.ulpin,
                action = it.action
            )
        }
        
        if (remoteLogs.isNotEmpty()) {
            supabase.postgrest["audit_logs"].insert(remoteLogs)
            // Optionally clear local logs after successful sync
            // database.auditLogDao().clearAll()
        }
    }

    suspend fun pullParcelsFromRemote() = withContext(Dispatchers.IO) {
        val remoteParcels = supabase.postgrest["parcels"]
            .select()
            .decodeList<RemoteParcel>()

        val localEntities = remoteParcels.map {
            ParcelEntity(
                ulpin = it.ulpin,
                state = it.state,
                district = it.district,
                parcelDataJson = it.parcel_data.toString()
            )
        }
        
        if (localEntities.isNotEmpty()) {
            database.parcelDao().insertParcels(localEntities)
        }
    }
}
