package com.govtech.landstack.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "data_conflicts",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DataConflictEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val category: String,
    val severity: String,
    val status: String,
    val details: String?,
    val mismatchedValues: String?, // JSON string
    val assignedTo: String?,
    val resolvedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
