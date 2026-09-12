package com.govtech.landstack.data.repository

import com.govtech.landstack.data.model.Zoning
import com.govtech.landstack.data.model.Building
import kotlinx.coroutines.flow.Flow

interface PlanningRepository {
    fun getZoning(ulpin: String): Flow<Zoning?>
    fun getBuildingPermission(ulpin: String): Flow<Building?>
}
