package com.govtech.landstack.feature.myproperty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.data.local.ParcelEntity
import com.govtech.landstack.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.jan.supabase.postgrest.postgrest

@HiltViewModel
class MyPropertyViewModel @Inject constructor(
    private val database: LandStackDatabase,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _parcels = MutableStateFlow<UiState<List<ParcelEntity>>>(UiState.Loading)
    val parcels: StateFlow<UiState<List<ParcelEntity>>> = _parcels.asStateFlow()

    init {
        loadMyProperties()
    }

    private fun loadMyProperties() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId == null) {
                    _parcels.value = UiState.Error("User not logged in")
                    return@launch
                }

                // First find owners that match this user
                val myOwners = database.relationalDao().getTotalOwnersCount() // We need a way to get owners by user_id
                // We don't have a direct query in RelationalDao for owners by user_id.
                // Let's get all parcels and filter. Or just fetch from Supabase if local doesn't have it.
                // Actually, the app syncs all owners, but if it doesn't, let's just do a remote fetch.
                val remoteOwners = supabase.postgrest["parcel_owners"]
                    .select {
                        filter {
                            eq("user_id", userId)
                            // We should also check effective_to is null, but we can do it in memory
                        }
                    }.decodeList<com.govtech.landstack.data.remote.RemoteParcelOwner>()

                val activeUlpinList = remoteOwners.filter { it.effectiveTo == null }.map { it.ulpin }.distinct()

                val localParcels = database.parcelDao().getAllParcelsSync()
                val myParcels = localParcels.filter { activeUlpinList.contains(it.ulpin) }

                if (myParcels.isEmpty()) {
                    _parcels.value = UiState.Empty
                } else {
                    _parcels.value = UiState.Success(myParcels)
                }
            } catch (e: Exception) {
                _parcels.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
