package com.govtech.landstack.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.govtech.landstack.data.local.BuildingPermissionEntity
import com.govtech.landstack.data.local.ParcelDao
import com.govtech.landstack.data.local.RelationalDao
import com.govtech.landstack.data.local.ZoningCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import com.govtech.landstack.data.repository.SyncRepository
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    parcelDao: ParcelDao,
    relationalDao: RelationalDao,
    private val dataConflictDao: com.govtech.landstack.data.local.DataConflictDao,
    private val applicationDao: com.govtech.landstack.data.local.ApplicationDao,
    private val syncRepository: SyncRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            syncRepository.pullParcelsFromRemote()
        }
    }

    val zoningCounts: StateFlow<List<ZoningCount>> = parcelDao.getZoningCounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingApprovals: StateFlow<List<BuildingPermissionEntity>> = relationalDao.getPendingPermissions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeApplications: StateFlow<Int> = applicationDao.getActiveApplicationsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalParcels: StateFlow<Int> = parcelDao.getTotalParcelsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val activeEncumbrances: StateFlow<Int> = relationalDao.getActiveEncumbrancesCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalOwners: StateFlow<Int> = relationalDao.getTotalOwnersCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val openConflicts: StateFlow<Int> = dataConflictDao.getOpenConflictsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val allConflicts: StateFlow<List<com.govtech.landstack.data.local.DataConflictEntity>> = dataConflictDao.getAllConflicts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allApplications: StateFlow<List<com.govtech.landstack.data.local.ApplicationEntity>> = applicationDao.getAllApplications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
