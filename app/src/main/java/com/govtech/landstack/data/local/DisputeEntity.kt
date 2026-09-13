package com.govtech.landstack.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "disputes",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DisputeEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val caseNumber: String?,
    val disputeType: String,
    val description: String?,
    val source: String,
    val sourceReference: String?,
    val effectiveDate: String?,
    val isStayOrder: Boolean,
    val status: String,
    val partiesInvolved: String,
    val filedBy: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
