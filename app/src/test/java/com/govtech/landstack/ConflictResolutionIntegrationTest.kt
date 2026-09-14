package com.govtech.landstack

import android.content.Context
import androidx.room.Room
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.data.local.ParcelEntity
import com.govtech.landstack.data.repository.SyncRepository
import com.govtech.landstack.feature.parceldetail.ParcelDetailViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.CodeVerifierCache
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class ConflictResolutionIntegrationTest {

    private lateinit var db: LandStackDatabase
    private lateinit var syncRepository: SyncRepository
    private lateinit var context: Context
    private lateinit var supabase: SupabaseClient
    
    @Before
    fun setup() {
        context = org.robolectric.RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, LandStackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            
        supabase = createSupabaseClient(
            supabaseUrl = "https://zkfaxgjswooptjpsftjz.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InprZmF4Z2pzd29vcHRqcHNmdGp6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODkxMjY3OTMsImV4cCI6MjEwNDcwMjc5M30.sk0CG0JBtsPHtQd8Wq11XFyRNUsfXr8ae5wXOD2U3Dw"
        ) {
            install(Postgrest)
            install(Auth) {
                sessionManager = object : SessionManager {
                    private var currentSession: UserSession? = null
                    override suspend fun loadSession(): UserSession? = currentSession
                    override suspend fun saveSession(session: UserSession) { currentSession = session }
                    override suspend fun deleteSession() { currentSession = null }
                }
                codeVerifierCache = object : CodeVerifierCache {
                    override suspend fun loadCodeVerifier(): String? = null
                    override suspend fun saveCodeVerifier(codeVerifier: String) {}
                    override suspend fun deleteCodeVerifier() {}
                }
            }
        }
        
        Dispatchers.setMain(Dispatchers.Unconfined)
        syncRepository = SyncRepository(context, db, supabase)
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun runServiceRoleCommand(command: String): String {
        val process = ProcessBuilder("/bin/sh", "-c", command).start()
        val out = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return out
    }

    @Test
    fun testKeepMineAndKeepTheirs() = runBlocking {
        val email = "officer_test_${Random.nextInt(1000000)}@test.com"
        val password = "password123"
        val srKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InprZmF4Z2pzd29vcHRqcHNmdGp6Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4OTEyNjc5MywiZXhwIjoyMTA0NzAyNzkzfQ.5PxbRQ-ytx_svKp3hpDXi4IVBNif5fS__NwLV2tJ3Ag"
        val url = "https://zkfaxgjswooptjpsftjz.supabase.co"
        
        val createCmd = """curl -s -X POST "$url/auth/v1/admin/users" -H "apikey: $srKey" -H "Authorization: Bearer $srKey" -H "Content-Type: application/json" -d '{"email": "$email", "password": "$password", "email_confirm": true}'"""
        val createRes = runServiceRoleCommand(createCmd)
        val officerId = Regex("\"id\":\"([^\"]+)").find(createRes)?.groupValues?.get(1)
        assertNotNull("Could not parse officer ID", officerId)
        
        runServiceRoleCommand("""curl -s -X PATCH "$url/rest/v1/profiles?id=eq.$officerId" -H "apikey: $srKey" -H "Authorization: Bearer $srKey" -H "Content-Type: application/json" -d '{"role": "Land Officer"}'""")
        
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        
        val viewModel = ParcelDetailViewModel(
            parcelDao = db.parcelDao(), 
            relationalDao = db.relationalDao(), 
            pendingConflictDao = db.pendingConflictDao(), 
            auditLogDao = db.auditLogDao(), 
            documentDao = object : com.govtech.landstack.data.local.DocumentDao {
                override suspend fun insertDocuments(docs: List<com.govtech.landstack.data.local.DocumentEntity>) {}
                override fun getDocumentsByUlpin(ulpin: String) = kotlinx.coroutines.flow.flowOf(emptyList<com.govtech.landstack.data.local.DocumentEntity>())
            },
            disputeDao = object : com.govtech.landstack.data.local.DisputeDao {
                override suspend fun insertDisputes(disputes: List<com.govtech.landstack.data.local.DisputeEntity>) {}
                override fun getDisputesByUlpin(ulpin: String) = kotlinx.coroutines.flow.flowOf(emptyList<com.govtech.landstack.data.local.DisputeEntity>())
            },
            restrictionDao = object : com.govtech.landstack.data.local.RestrictionDao {
                override suspend fun insertRestrictions(restrictions: List<com.govtech.landstack.data.local.RestrictionEntity>) {}
                override fun getRestrictionsByUlpin(ulpin: String) = kotlinx.coroutines.flow.flowOf(emptyList<com.govtech.landstack.data.local.RestrictionEntity>())
                override suspend fun getRestrictionsByUlpinSync(ulpin: String) = emptyList<com.govtech.landstack.data.local.RestrictionEntity>()
            },
            syncRepository = syncRepository, 
            supabase = supabase
        )
        
        suspend fun executeScenario(ulpin: String, keepMine: Boolean) {
            println("--- Starting Scenario for $ulpin (keepMine=$keepMine) ---")
            
            val setupCmd = """curl -s -X POST "$url/rest/v1/parcels" -H "apikey: $srKey" -H "Authorization: Bearer $srKey" -H "Content-Type: application/json" -H "Prefer: return=representation" -d '{"ulpin": "$ulpin", "state": "MH", "district": "Mumbai", "parcel_data": {}}'"""
            runServiceRoleCommand(setupCmd)
            
            val getUpdatedCmd = """curl -s -X GET "$url/rest/v1/parcels?ulpin=eq.$ulpin&select=updated_at" -H "apikey: $srKey" -H "Authorization: Bearer $srKey""""
            val baseUpdatedAtRes = runServiceRoleCommand(getUpdatedCmd)
            val baseUpdatedAt = Regex("\"updated_at\":\"([^\"]+)").find(baseUpdatedAtRes)?.groupValues?.get(1) ?: "1970-01-01T00:00:00Z"
            
            val localParcel = ParcelEntity(ulpin = ulpin, state = "MH", district = "Pune", zoningClassification = "Residential", permittedUse = "Housing", parcelDataJson = "{\"test\": \"local edit\"}", updatedAt = baseUpdatedAt)
            db.parcelDao().insertParcels(listOf(localParcel))
            
            kotlinx.coroutines.delay(1000)
            val remoteEditCmd = """curl -s -X PATCH "$url/rest/v1/parcels?ulpin=eq.$ulpin" -H "apikey: $srKey" -H "Authorization: Bearer $srKey" -H "Content-Type: application/json" -d '{"district": "Delhi", "parcel_data": {"test": "remote edit"}}'"""
            runServiceRoleCommand(remoteEditCmd)
            
            val pushResult = syncRepository.pushSingleParcel(ulpin)
            assert(pushResult is com.govtech.landstack.data.repository.SyncResult.Conflict)
            
            val conflictResult = pushResult as com.govtech.landstack.data.repository.SyncResult.Conflict
            val conflictMsg = conflictResult.message ?: "Unknown conflict"
            db.pendingConflictDao().insertConflict(
                com.govtech.landstack.data.local.PendingConflictEntity(
                    ulpin = localParcel.ulpin,
                    localDataJson = localParcel.parcelDataJson,
                    baseUpdatedAt = localParcel.updatedAt,
                    message = conflictMsg
                )
            )
            
            val pendingConflict = db.pendingConflictDao().getConflict(ulpin).first()
            assertNotNull("Pending conflict was not inserted in Room!", pendingConflict)
            println("Conflict correctly logged in Room: ${pendingConflict?.message}")
            
            // Call loadParcel to trigger _pendingConflict updates internally in ViewModel
            viewModel.loadParcel(ulpin)
            kotlinx.coroutines.delay(1000) // Wait for coroutine inside ViewModel to update StateFlow
            
            println("Current pendingConflict in ViewModel: ${viewModel.pendingConflict.value}")
            viewModel.resolveConflict(keepMine)
            
            kotlinx.coroutines.delay(2000) // Wait for resolution and audit log push
            
            val auditLogs = db.auditLogDao().getAllLogsSync().filter { it.ulpin == ulpin }
            assert(auditLogs.isNotEmpty())
            val lastLog = auditLogs.last()
            println("Audit log created: ${lastLog.action}")
            
            val expectedAction = if (keepMine) "conflict_resolved_kept_local" else "conflict_resolved_kept_remote"
            assertEquals(expectedAction, lastLog.action)
            
            val finalGetCmd = """curl -s -X GET "$url/rest/v1/parcels?ulpin=eq.$ulpin&select=district,parcel_data" -H "apikey: $srKey" -H "Authorization: Bearer $srKey""""
            val finalRes = runServiceRoleCommand(finalGetCmd)
            println("Final remote parcel state: $finalRes")
            
            val finalLocal = db.parcelDao().getParcelSync(ulpin)
            println("Final local parcel state: District = ${finalLocal?.district}, Data = ${finalLocal?.parcelDataJson}")
        }
        
        executeScenario("ULPIN_KEEP_MINE_${Random.nextInt(1000000)}", keepMine = true)
        println("\n=======================================================\n")
        executeScenario("ULPIN_KEEP_THEIRS_${Random.nextInt(1000000)}", keepMine = false)
    }
}
