package com.govtech.landstack.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "applications",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ApplicationEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val applicantId: String,
    val applicationType: String,
    val currentStage: String,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "application_stages",
    foreignKeys = [
        ForeignKey(
            entity = ApplicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["applicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ApplicationStageEntity(
    @PrimaryKey
    val id: Long,
    val applicationId: Long,
    val stage: String,
    val status: String,
    val updatedBy: String?,
    val remarks: String?,
    val timestamp: Instant
)
