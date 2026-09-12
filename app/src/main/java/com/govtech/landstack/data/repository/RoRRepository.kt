package com.govtech.landstack.data.repository

import com.govtech.landstack.data.model.RecordOfRights
import kotlinx.coroutines.flow.Flow

interface RoRRepository {
    fun getRoR(ulpin: String): Flow<RecordOfRights?>
}
