package com.govtech.landstack.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "property_transactions")
data class PropertyTransactionEntity(
    @PrimaryKey
    val id: Long,
    val ulpin: String,
    @ColumnInfo(name = "buyer_name") val buyerName: String,
    @ColumnInfo(name = "buyer_user_id") val buyerUserId: String?,
    @ColumnInfo(name = "seller_owner_id") val sellerOwnerId: Long?,
    val status: String,
    @ColumnInfo(name = "initiated_by") val initiatedBy: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)
