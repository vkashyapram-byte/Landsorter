package com.govtech.landstack.data.repository

import com.govtech.landstack.data.local.ParcelDao
import com.govtech.landstack.data.model.Parcel
import com.govtech.landstack.data.model.BaseLayer
import com.govtech.landstack.data.model.RecordOfRights
import com.govtech.landstack.data.model.Registration
import com.govtech.landstack.data.model.Zoning
import com.govtech.landstack.data.model.Building
import com.govtech.landstack.data.model.Taxation
import com.govtech.landstack.data.model.Utilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class OfflineCadastralRepository(private val dao: ParcelDao) : CadastralRepository {
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun getBaseLayer(ulpin: String): Flow<BaseLayer?> {
        return dao.getParcel(ulpin).map { entity ->
            entity?.let { json.decodeFromString<Parcel>(it.parcelDataJson).base }
        }
    }

    override fun getAllBaseLayers(): Flow<List<BaseLayer>> {
        return dao.getAllParcels().map { entities ->
            entities.map { json.decodeFromString<Parcel>(it.parcelDataJson).base }
        }
    }
}

class OfflineRoRRepository(private val dao: ParcelDao) : RoRRepository {
    private val json = Json { ignoreUnknownKeys = true }
    override fun getRoR(ulpin: String): Flow<RecordOfRights?> {
        return dao.getParcel(ulpin).map { entity ->
            entity?.let { json.decodeFromString<Parcel>(it.parcelDataJson).essential.ror }
        }
    }
}

class OfflineRegistrationRepository(private val dao: ParcelDao) : RegistrationRepository {
    private val json = Json { ignoreUnknownKeys = true }
    override fun getRegistration(ulpin: String): Flow<Registration?> {
        return dao.getParcel(ulpin).map { entity ->
            entity?.let { json.decodeFromString<Parcel>(it.parcelDataJson).essential.registration }
        }
    }
}

class OfflinePlanningRepository(private val dao: ParcelDao) : PlanningRepository {
    private val json = Json { ignoreUnknownKeys = true }
    override fun getZoning(ulpin: String): Flow<Zoning?> {
        return dao.getParcel(ulpin).map { entity ->
            entity?.let { json.decodeFromString<Parcel>(it.parcelDataJson).essential.zoning }
        }
    }

    override fun getBuildingPermission(ulpin: String): Flow<Building?> {
        return dao.getParcel(ulpin).map { entity ->
            entity?.let { json.decodeFromString<Parcel>(it.parcelDataJson).essential.building }
        }
    }
}

class OfflineMunicipalRepository(private val dao: ParcelDao) : MunicipalRepository {
    private val json = Json { ignoreUnknownKeys = true }
    override fun getTaxation(ulpin: String): Flow<Taxation?> {
        return dao.getParcel(ulpin).map { entity ->
            entity?.let { json.decodeFromString<Parcel>(it.parcelDataJson).additional.taxation }
        }
    }

    override fun getUtilities(ulpin: String): Flow<Utilities?> {
        return dao.getParcel(ulpin).map { entity ->
            entity?.let { json.decodeFromString<Parcel>(it.parcelDataJson).additional.utilities }
        }
    }
}
