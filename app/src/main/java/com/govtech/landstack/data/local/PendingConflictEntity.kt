package com.govtech.landstack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_conflicts")
data class PendingConflictEntity(
    @PrimaryKey val ulpin: String,
    val localDataJson: String,
    val baseUpdatedAt: String,
    val message: String
)
