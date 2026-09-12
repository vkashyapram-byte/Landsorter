package com.govtech.landstack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val userRole: String,
    val ulpin: String,
    val action: String // e.g., "VIEW", "EDIT"
)
