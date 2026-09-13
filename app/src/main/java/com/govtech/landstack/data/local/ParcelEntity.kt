package com.govtech.landstack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey val ulpin: String,
    val state: String,
    val district: String,
    val zoningClassification: String,
    val permittedUse: String,
    val parcelDataJson: String, // Serialized full Parcel model
    val updatedAt: String
)
