package com.govtech.landstack.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "service_requests")
data class ServiceRequestEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String?,
    @ColumnInfo(name = "citizen_user_id") val citizenUserId: String,
    @ColumnInfo(name = "request_type") val requestType: String,
    val description: String?,
    val status: String,
    @ColumnInfo(name = "handled_by") val handledBy: String?,
    @ColumnInfo(name = "handled_at") val handledAt: Instant?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)
