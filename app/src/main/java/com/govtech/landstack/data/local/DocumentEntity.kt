package com.govtech.landstack.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DocumentEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val docType: String,
    val filePath: String,
    val uploadedBy: String,
    val verificationStatus: String,
    val ocrExtractedText: String?,
    val ocrProcessedAt: Instant?,
    val verifiedBy: String?,
    val verifiedAt: Instant?,
    val registrationId: Long?,
    val createdAt: Instant,
    val updatedAt: Instant
)
