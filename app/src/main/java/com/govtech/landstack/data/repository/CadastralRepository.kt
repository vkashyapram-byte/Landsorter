package com.govtech.landstack.data.repository

import com.govtech.landstack.data.model.BaseLayer
import kotlinx.coroutines.flow.Flow

interface CadastralRepository {
    fun getBaseLayer(ulpin: String): Flow<BaseLayer?>
    fun getAllBaseLayers(): Flow<List<BaseLayer>>
}
