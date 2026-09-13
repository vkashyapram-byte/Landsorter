package com.govtech.landstack.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "parcel_owners",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OwnerEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val ownerName: String,
    val userId: String?,
    val khataNumber: String?,
    val rightType: String?,
    val ownershipShare: Double?,
    val effectiveFrom: Instant,
    val effectiveTo: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "building_permissions",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BuildingPermissionEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val sanctionNumber: String?,
    val approvedBuiltUpArea: Double?,
    val floors: Int?,
    val status: String,
    val submittedBy: String?,
    val decidedBy: String?,
    val decidedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "parcel_encumbrances",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EncumbranceEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val lender: String?,
    val loanAmount: Double?,
    val lienStatus: String?,
    val validFrom: Instant?,
    val validTo: Instant?,
    val resolvedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "parcel_tax_records",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaxRecordEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val taxYear: Int?,
    val assessedValue: Double?,
    val annualTax: Double?,
    val paymentStatus: String?,
    val arrears: Double?,
    val paidAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "parcel_registrations",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RegistrationEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val deedNumber: String?,
    val registrationDate: String?, // Using String to avoid custom LocalDate converters for now
    val transactionType: String?,
    val stampDutyPaid: Double?,
    val subOffice: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "parcel_restrictions",
    foreignKeys = [
        ForeignKey(
            entity = ParcelEntity::class,
            parentColumns = ["ulpin"],
            childColumns = ["ulpin"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RestrictionEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    val restrictionType: String,
    val description: String?,
    val source: String,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
