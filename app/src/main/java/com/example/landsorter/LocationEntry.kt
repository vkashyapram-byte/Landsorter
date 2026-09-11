package com.example.landsorter

import kotlinx.serialization.Serializable

@Serializable
data class LocationEntry(
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val description: String? = null
)
