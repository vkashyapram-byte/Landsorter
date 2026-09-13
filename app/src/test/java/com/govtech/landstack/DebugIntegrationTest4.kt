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
class DebugIntegrationTest4 {
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
        syncRepository.pullEncumbrances()
        syncRepository.pullTaxRecords()
        syncRepository.pullRegistrations()
        
        val enc = db.relationalDao().getEncumbrancesForParcel("all_test_17117").first()
        println("enc in db for all_test_17117: ${enc.size}")
        
        val tax = db.relationalDao().getTaxRecordsForParcel("all_test_17117").first()
        println("tax in db for all_test_17117: ${tax.size}")
        
        val reg = db.relationalDao().getRegistrationsForParcel("all_test_17117").first()
        println("reg in db for all_test_17117: ${reg.size}")
    }
}
