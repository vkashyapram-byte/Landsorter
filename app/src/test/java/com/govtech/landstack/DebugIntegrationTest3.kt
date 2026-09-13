package com.govtech.landstack

import android.content.Context
import androidx.room.Room
import com.govtech.landstack.data.local.LandStackDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.CodeVerifierCache
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserSession
import com.govtech.landstack.data.repository.SyncRepository
import com.govtech.landstack.data.repository.SyncResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class DebugIntegrationTest3 {
    private lateinit var db: LandStackDatabase
    private lateinit var syncRepository: SyncRepository
    private lateinit var context: Context
    private lateinit var supabase: SupabaseClient

    @Before
    fun setup() {
        supabase = createSupabaseClient(
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
        context = org.robolectric.RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, LandStackDatabase::class.java).allowMainThreadQueries().build()
        syncRepository = SyncRepository(context, db, supabase)
    }

    @Test
    fun checkData() = runBlocking {
        supabase.auth.signInWith(Email) { email = "test_officer@test.com"; password = "password1234" }
        
        syncRepository.pullParcelsFromRemote()
        syncRepository.pullOwners()
        
        val owners = db.relationalDao().getOwnersForParcel("mut_test_14935").first()
        println("owners in db for mut_test_14935:")
        owners.forEach {
            println("owner: ${it.ownerName}, from: ${it.effectiveFrom}, to: ${it.effectiveTo}")
        }
    }
}
