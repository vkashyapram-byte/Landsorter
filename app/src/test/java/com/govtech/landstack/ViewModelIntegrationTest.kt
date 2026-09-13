package com.govtech.landstack

import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.feature.parceldetail.ParcelDetailViewModel
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class ViewModelIntegrationTest {

    private lateinit var db: LandStackDatabase
    private lateinit var syncRepository: SyncRepository
    private lateinit var viewModel: ParcelDetailViewModel
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
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        
        db = Room.inMemoryDatabaseBuilder(context, LandStackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            
        syncRepository = SyncRepository(context, db, supabase)
        viewModel = ParcelDetailViewModel(
            parcelDao = db.parcelDao(),
            relationalDao = db.relationalDao(),
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
            },
            pendingConflictDao = object : com.govtech.landstack.data.local.PendingConflictDao {
                override suspend fun insertConflict(conflict: com.govtech.landstack.data.local.PendingConflictEntity) {}
                override suspend fun deleteConflict(ulpin: String) {}
                override fun getConflict(ulpin: String) = kotlinx.coroutines.flow.flowOf<com.govtech.landstack.data.local.PendingConflictEntity?>(null)
            },
            auditLogDao = object : com.govtech.landstack.data.local.AuditLogDao {
                override suspend fun insertLog(log: com.govtech.landstack.data.local.AuditLogEntity) {}
                override fun getAllLogs() = kotlinx.coroutines.flow.flowOf(emptyList<com.govtech.landstack.data.local.AuditLogEntity>())
                override suspend fun getAllLogsSync(): List<com.govtech.landstack.data.local.AuditLogEntity> = emptyList()
            },
            syncRepository = syncRepository,
            supabase = supabase
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testMutTest14935Data() = runBlocking {
        println("--- START VIEWMODEL TEST ---")
        
        supabase.auth.signInWith(Email) {
            email = "test_officer@test.com"
            password = "password1234"
        }
        
        // 1. Sync down all data via pull functions
        syncRepository.pullParcelsFromRemote()
        syncRepository.pullOwners()
        syncRepository.pullPermissions()
        syncRepository.pullEncumbrances()
        syncRepository.pullTaxRecords()
        syncRepository.pullRegistrations()
        
        println("Pulled data to local DB successfully.")
        
        // 2. Load it through ViewModel
        val targetUlpin = "mut_test_14935"
        viewModel.loadParcel(targetUlpin)
        
        // Wait for the coroutines to collect from Room
        var retries = 0
        while (viewModel.owners.value.isEmpty() && retries < 20) {
            delay(100)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            retries++
        }
        
        // 3. Assert activeOwners and historyOwners
        val allOwners = viewModel.owners.value
        println("Total owners for $targetUlpin: ${allOwners.size}")
        allOwners.forEach { println(" - Owner: ${it.ownerName}, EffectiveTo: ${it.effectiveTo}") }
        
        val activeOwners = allOwners.filter { it.effectiveTo == null }
        val historyOwners = allOwners.filter { it.effectiveTo != null }
        
        val activeNames = activeOwners.map { it.ownerName }.toSet()
        val historyNames = historyOwners.map { it.ownerName }.toSet()
        
        assertEquals("Expected exactly Dave as active owner", setOf("Dave"), activeNames)
        assertEquals("Expected exactly Bob and Charlie as history owners", setOf("Bob", "Charlie"), historyNames)
        
        println("Assertion PASSED: Active owners exactly [Dave]. History owners exactly [Bob, Charlie].")
        
        // 4. Assert on other sections
        val permits = viewModel.permissions.value
        val encumbrances = viewModel.encumbrances.value
        val taxes = viewModel.taxRecords.value
        val registrations = viewModel.registrations.value
        
        println("Permits count: ${permits.size}")
        permits.forEach { println(" - Permit: ${it.sanctionNumber} | Status: ${it.status}") }
        
        println("Encumbrances count: ${encumbrances.size}")
        encumbrances.forEach { println(" - Encumbrance: ${it.lender} | Active: ${it.resolvedAt == null}") }
        
        println("Taxes count: ${taxes.size}")
        taxes.forEach { println(" - Tax: Year ${it.taxYear} | Status: ${it.paymentStatus}") }
        
        println("Registrations count: ${registrations.size}")
        registrations.forEach { println(" - Reg: ${it.deedNumber} | Type: ${it.transactionType}") }
        
        // Let's assert on anything that has data
        if (permits.isNotEmpty()) {
            val hasApplied = permits.any { it.status == "applied" }
            val hasApproved = permits.any { it.status == "approved" }
            assertTrue("Expected either applied or approved permit", hasApplied || hasApproved)
            println("Assertion PASSED: Permits exist and have valid statuses.")
        }
        
        if (encumbrances.isNotEmpty()) {
            assertTrue("Expected non-empty lender for encumbrance", encumbrances.all { !it.lender.isNullOrBlank() })
            println("Assertion PASSED: Encumbrances exist and have lenders.")
        }
        
        if (taxes.isNotEmpty()) {
            assertTrue("Expected non-null tax year", taxes.all { it.taxYear != null })
            println("Assertion PASSED: Taxes exist and have years.")
        }
        
        if (registrations.isNotEmpty()) {
            assertTrue("Expected non-null deed number", registrations.all { !it.deedNumber.isNullOrBlank() })
            println("Assertion PASSED: Registrations exist and have deed numbers.")
        }

        println("--- END VIEWMODEL TEST ---")
    }

    @Test
    fun testAllTest17117Data() = runBlocking {
        println("--- START VIEWMODEL TEST 2 ---")
        
        supabase.auth.signInWith(Email) {
            email = "test_officer@test.com"
            password = "password1234"
        }
        
        // 1. Sync down all data via pull functions
        syncRepository.pullParcelsFromRemote()
        syncRepository.pullEncumbrances()
        syncRepository.pullTaxRecords()
        syncRepository.pullRegistrations()
        
        println("Pulled data to local DB successfully.")
        
        // 2. Load it through ViewModel
        val targetUlpin = "all_test_17117"
        viewModel.loadParcel(targetUlpin)
        
        // Wait for the coroutines to collect from Room (poll until we have data in encumbrances, tax, reg)
        var retries = 0
        while ((viewModel.encumbrances.value.isEmpty() || viewModel.taxRecords.value.isEmpty() || viewModel.registrations.value.isEmpty()) && retries < 20) {
            delay(100)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            retries++
        }
        
        // 3. Assert on sections
        val encumbrances = viewModel.encumbrances.value
        val taxes = viewModel.taxRecords.value
        val registrations = viewModel.registrations.value
        
        println("Encumbrances count: ${encumbrances.size}")
        encumbrances.forEach { println(" - Encumbrance: ${it.lender} | Active: ${it.resolvedAt == null}") }
        
        println("Taxes count: ${taxes.size}")
        taxes.forEach { println(" - Tax: Year ${it.taxYear} | Status: ${it.paymentStatus}") }
        
        println("Registrations count: ${registrations.size}")
        registrations.forEach { println(" - Reg: ${it.deedNumber} | Type: ${it.transactionType}") }
        
        assertEquals("Expected exactly 1 encumbrance", 1, encumbrances.size)
        assertEquals("Expected exactly 1 tax record", 1, taxes.size)
        assertEquals("Expected exactly 1 registration", 1, registrations.size)

        assertEquals("Expected encumbrance lender to be 'Bank'", "Bank", encumbrances[0].lender)
        assertEquals("Expected tax year to be 2026", 2026, taxes[0].taxYear)
        assertEquals("Expected registration deed_number to be 'D-123'", "D-123", registrations[0].deedNumber)

        println("Assertion PASSED: Encumbrance, Tax, and Registration counts are 1 with expected values.")
        println("--- END VIEWMODEL TEST 2 ---")
    }
}
