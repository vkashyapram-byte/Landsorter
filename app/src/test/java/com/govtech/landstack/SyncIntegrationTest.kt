package com.govtech.landstack
import kotlinx.coroutines.flow.first

import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.data.local.ParcelEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.CodeVerifierCache
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserSession
import com.govtech.landstack.data.repository.SyncRepository
import com.govtech.landstack.data.sync.SyncManager
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.Serializable

@Serializable
data class RemoteParcelTest(
    val ulpin: String,
    val state: String,
    val district: String,
    val parcel_data: JsonElement? = null
)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class SyncIntegrationTest {

    private lateinit var db: LandStackDatabase
    private lateinit var syncRepository: SyncRepository
    private lateinit var context: Context
    private lateinit var supabase: SupabaseClient
    private val ulpin = "A123"

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
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testDoubleEditSync() = runBlocking {
        println("--- START TEST ---")
        
        // Log in as real Land Officer account to authenticate the session for RLS
        supabase.auth.signInWith(Email) {
            email = "test_officer@test.com"
            password = "password1234"
        }
        println("Logged in as Land Officer successfully.")
        
        // Setup initial parcel in remote and local
        try {
            supabase.postgrest["parcels"].delete { filter { eq("ulpin", ulpin) } }
        } catch (e: Exception) {}
        
        val initialParcel = ParcelEntity(ulpin, "TestState", "InitialDistrict", "Residential", "Housing", "{}", "2026-09-01T00:00:00Z")
        db.parcelDao().insertParcels(listOf(initialParcel))
        syncRepository.pushSingleParcel(ulpin)
        
        // 1. Simulate offline: edit parcel A123's district to "First Edit"
        db.parcelDao().insertParcels(listOf(ParcelEntity(ulpin, "TestState", "First Edit", "Residential", "Housing", "{}", "2026-09-01T00:00:00Z")))
        SyncManager.enqueueGranularSync(context, "parcel", ulpin, "upsert")
        
        val roomFirst = db.parcelDao().getParcelSync(ulpin)
        println("1. Room output after first edit: district = '${roomFirst?.district}'")
        
        // 2. Still offline: edit A123's district again to "Second Edit"
        db.parcelDao().insertParcels(listOf(ParcelEntity(ulpin, "TestState", "Second Edit", "Residential", "Housing", "{}", "2026-09-01T00:00:00Z")))
        SyncManager.enqueueGranularSync(context, "parcel", ulpin, "upsert")
        
        val roomSecond = db.parcelDao().getParcelSync(ulpin)
        println("2. Room output after second edit: district = '${roomSecond?.district}'")
        
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork("sync_parcel_${ulpin}_upsert").get()
        println("2. WorkManager WorkInfo count for sync_parcel_${ulpin}_upsert: ${workInfos.size}")
        if (workInfos.isNotEmpty()) {
            println("2. WorkInfo State: ${workInfos[0].state}")
        }
        
        // 3. Reconnect and let the worker fire (execute all enqueued work)
        syncRepository.pushSingleParcel(ulpin)
        
        val remoteResult = supabase.postgrest["parcels"]
            .select(columns = Columns.list("ulpin, state, district")) {
                filter { eq("ulpin", ulpin) }
            }.decodeSingle<RemoteParcelTest>()
            
        println("3. Supabase actual raw value returned: district = '${remoteResult.district}'")
        
        // 4. Confirm it reads "Second Edit"
        assertEquals("Second Edit", remoteResult.district)
        println("4. Confirmed Supabase reads 'Second Edit'.")
        println("--- END TEST ---")
    }

    
    @Test
    fun testGranularSyncPaths() = runBlocking {
        println("=== TEST 1: RPC Path (recordOwnershipMutation) ===")
        
        // Log in as real Land Officer account
        supabase.auth.signInWith(Email) {
            email = "test_officer@test.com"
            password = "password1234"
        }
        
        val testUlpin = "mut_test_14935"
        val viewModel = com.govtech.landstack.feature.parceldetail.ParcelDetailViewModel(
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
        
        // ensure parcel exists locally
        syncRepository.pullParcelsFromRemote()
        syncRepository.pullOwners()
        syncRepository.pullEncumbrances()
        
        val initialOwners = db.relationalDao().getOwnersForParcel(testUlpin).first()
        println("Room initial active owners: " + initialOwners.filter { it.effectiveTo == null }.map { it.ownerName })
        
        // Action: record mutation locally
        viewModel.recordOwnershipMutation(
            context = context,
            ulpin = testUlpin,
            name = "Test Mutation Owner",
            khata = "TM-999",
            rightType = "Freehold",
            share = 1.0
        )
        
        // Room before sync
        val roomBeforeSync = db.relationalDao().getOwnersForParcel(testUlpin).first()
        println("Room before sync:")
        roomBeforeSync.forEach { println(" - ID: ${it.id}, Name: ${it.ownerName}, EffectiveTo: ${it.effectiveTo}") }
        
        val newTempOwner = roomBeforeSync.find { it.id < 0 }!!
        
        // Sync manually (simulating SyncWorker)
        val res = syncRepository.pushOwnershipMutation(
            newTempOwner.ulpin,
            listOf(
                com.govtech.landstack.data.remote.RemoteParcelOwner(
                    ulpin = newTempOwner.ulpin,
                    ownerName = newTempOwner.ownerName,
                    userId = newTempOwner.userId,
                    khataNumber = newTempOwner.khataNumber,
                    rightType = newTempOwner.rightType,
                    ownershipShare = newTempOwner.ownershipShare
                )
            )
        )
        println("Sync result: $res")
        if (res is com.govtech.landstack.data.repository.SyncResult.Success) {
            db.relationalDao().deleteOwnerSync(newTempOwner.id)
            syncRepository.pullOwners()
        }
        
        // Supabase after sync
        val supabaseData = supabase.postgrest["parcel_owners"]
            .select() { filter { eq("ulpin", testUlpin) } }
            .decodeList<kotlinx.serialization.json.JsonElement>().toString()
        println("Supabase after sync: $supabaseData")
        
        // Room after pull
        val roomAfterPull = db.relationalDao().getOwnersForParcel(testUlpin).first()
        println("Room after pull:")
        roomAfterPull.forEach { println(" - ID: ${it.id}, Name: ${it.ownerName}, EffectiveTo: ${it.effectiveTo}") }
        
        
        println("\n=== TEST 2: Plain Insert Path (addEncumbrance) ===")
        viewModel.addEncumbrance(
            context = context,
            ulpin = testUlpin,
            lender = "Test Bank",
            amount = 50000.0,
            status = "active"
        )
        
        // Room before sync
        val roomEncBeforeSync = db.relationalDao().getEncumbrancesForParcel(testUlpin).first()
        println("Room before sync:")
        roomEncBeforeSync.forEach { println(" - ID: ${it.id}, Lender: ${it.lender}") }
        
        val newTempEnc = roomEncBeforeSync.find { it.id < 0 }!!
        
        // Sync manually (simulating SyncWorker)
        val res2 = syncRepository.pushNewEncumbrance(
            com.govtech.landstack.data.remote.RemoteParcelEncumbrance(
                ulpin = newTempEnc.ulpin,
                lender = newTempEnc.lender,
                loanAmount = newTempEnc.loanAmount,
                lienStatus = newTempEnc.lienStatus,
                validFrom = newTempEnc.validFrom?.toString(),
                validTo = newTempEnc.validTo?.toString(),
                resolvedAt = newTempEnc.resolvedAt?.toString()
            )
        )
        println("Sync result: $res2")
        if (res2 is com.govtech.landstack.data.repository.SyncResult.Success) {
            db.relationalDao().deleteEncumbranceSync(newTempEnc.id)
            syncRepository.pullEncumbrances()
        }
        
        // Supabase after sync
        val supabaseData2 = supabase.postgrest["parcel_encumbrances"]
            .select() { filter { eq("ulpin", testUlpin) } }
            .decodeList<kotlinx.serialization.json.JsonElement>().toString()
        println("Supabase after sync: $supabaseData2")
        
        // Room after pull
        val roomEncAfterPull = db.relationalDao().getEncumbrancesForParcel(testUlpin).first()
        println("Room after pull:")
        roomEncAfterPull.forEach { println(" - ID: ${it.id}, Lender: ${it.lender}") }
    }
}
