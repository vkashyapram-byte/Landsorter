package com.govtech.landstack

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.CodeVerifierCache
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.govtech.landstack.data.remote.RemoteParcelOwner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class SupabaseDirectTest {

    @Test
    fun fetchOwners() = runBlocking {
        println("--- START SUPABASE QUERY ---")
        val supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth) {
                sessionManager = object : SessionManager {
                    override suspend fun loadSession(): UserSession? = null
                    override suspend fun saveSession(session: UserSession) {}
                    override suspend fun deleteSession() {}
                }
                codeVerifierCache = object : CodeVerifierCache {
                    override suspend fun loadCodeVerifier(): String? = null
                    override suspend fun saveCodeVerifier(codeVerifier: String) {}
                    override suspend fun deleteCodeVerifier() {}
                }
            }
        }
        
        supabase.auth.signInWith(Email) {
            email = "test_officer@test.com"
            password = "password1234"
        }
        
        try {
            val owners = supabase.postgrest["parcel_owners"].select { filter { eq("ulpin", "mut_test_14935") } }.decodeList<RemoteParcelOwner>()
            println("Total Owners fetched from Supabase: ${owners.size}")
            owners.forEach { println(" - Owner: ${it.ownerName}, EffectiveTo: ${it.effectiveTo}") }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
        
        println("--- END SUPABASE QUERY ---")
    }
}
