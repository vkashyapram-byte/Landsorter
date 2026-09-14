package com.govtech.landstack.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.data.local.PropertyTransactionEntity
import com.govtech.landstack.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val database: LandStackDatabase,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _transactions = MutableStateFlow<UiState<List<PropertyTransactionEntity>>>(UiState.Loading)
    val transactions: StateFlow<UiState<List<PropertyTransactionEntity>>> = _transactions.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId == null) {
                    _transactions.value = UiState.Error("User not logged in")
                    return@launch
                }
                
                database.propertyTransactionDao().getTransactionsForUser(userId).collect {
                    if (it.isEmpty()) {
                        _transactions.value = UiState.Empty
                    } else {
                        _transactions.value = UiState.Success(it)
                    }
                }

            } catch (e: Exception) {
                _transactions.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
