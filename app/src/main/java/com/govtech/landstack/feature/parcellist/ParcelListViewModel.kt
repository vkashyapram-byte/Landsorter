package com.govtech.landstack.feature.parcellist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.govtech.landstack.data.local.ParcelDao
import com.govtech.landstack.data.local.ParcelEntity
import com.govtech.landstack.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class ParcelListViewModel @Inject constructor(
    parcelDao: ParcelDao
) : ViewModel() {
    val parcels: StateFlow<UiState<List<ParcelEntity>>> = parcelDao.getAllParcels()
        .map { list ->
            if (list.isEmpty()) UiState.Empty else UiState.Success(list)
        }
        .onStart { emit(UiState.Loading) }
        .catch { e -> emit(UiState.Error(e.localizedMessage ?: "Unknown Error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
}
