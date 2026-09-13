package com.govtech.landstack.feature.gismap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.govtech.landstack.data.local.ParcelDao
import com.govtech.landstack.data.local.ParcelEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    parcelDao: ParcelDao
) : ViewModel() {
    val parcels: StateFlow<List<ParcelEntity>> = parcelDao.getAllParcels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
