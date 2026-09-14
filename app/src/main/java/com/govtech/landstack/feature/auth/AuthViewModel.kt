package com.govtech.landstack.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class UserProfile(
    val id: String,
    val role: String,
    val created_at: String
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        // Mock logic for DEBUG quick login
        if (com.govtech.landstack.BuildConfig.DEBUG && password == "TestPass123!") {
            val debugRole = when (email) {
                "citizen_test@landstack.test" -> "Citizen"
                "officer_test@landstack.test" -> "Land Officer"
                "revenue_officer@landstack.test" -> "Revenue Officer"
                "registration_officer@landstack.test" -> "Registration Officer"
                "surveyor@landstack.test" -> "Surveyor"
                "admin_test@landstack.test" -> "Admin"
                else -> null
            }
            if (debugRole != null) {
                _authState.value = AuthState.Success(debugRole)
                return
            }
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                fetchUserRole()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                // Automatically login after signup might require a delay or 
                // in Supabase, if email confirmations are disabled, it logs in automatically.
                fetchUserRole()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    
    fun checkSession() {
        viewModelScope.launch {
            if (supabase.auth.currentSessionOrNull() != null) {
                fetchUserRole()
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
            } catch (e: Exception) {
                // Ignore logout errors
            } finally {
                _authState.value = AuthState.Idle
                onComplete()
            }
        }
    }

    private suspend fun fetchUserRole() {
        try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                val profile = supabase.postgrest["profiles"]
                    .select {
                        filter {
                            eq("id", user.id)
                        }
                    }.decodeSingleOrNull<UserProfile>()
                
                _authState.value = AuthState.Success(profile?.role ?: "Citizen")
            } else {
                _authState.value = AuthState.Error("User not found")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to fetch profile")
        }
    }
}
