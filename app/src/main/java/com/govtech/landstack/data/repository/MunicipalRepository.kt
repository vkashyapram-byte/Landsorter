package com.govtech.landstack.data.repository

import com.govtech.landstack.data.model.Taxation
import com.govtech.landstack.data.model.Utilities
import kotlinx.coroutines.flow.Flow

interface MunicipalRepository {
    fun getTaxation(ulpin: String): Flow<Taxation?>
    fun getUtilities(ulpin: String): Flow<Utilities?>
}
