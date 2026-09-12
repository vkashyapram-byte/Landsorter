package com.govtech.landstack.di

import android.content.Context
import androidx.room.Room
import com.govtech.landstack.data.local.AuditLogDao
import com.govtech.landstack.data.local.LandStackDatabase
import com.govtech.landstack.data.local.ParcelDao
import com.govtech.landstack.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LandStackDatabase {
        return Room.databaseBuilder(
            context,
            LandStackDatabase::class.java,
            "landstack.db"
        ).build()
    }

    @Provides
    fun provideParcelDao(db: LandStackDatabase): ParcelDao = db.parcelDao()

    @Provides
    fun provideAuditLogDao(db: LandStackDatabase): AuditLogDao = db.auditLogDao()

    @Provides
    @Singleton
    fun provideCadastralRepository(dao: ParcelDao): CadastralRepository = OfflineCadastralRepository(dao)

    @Provides
    @Singleton
    fun provideRoRRepository(dao: ParcelDao): RoRRepository = OfflineRoRRepository(dao)

    @Provides
    @Singleton
    fun provideRegistrationRepository(dao: ParcelDao): RegistrationRepository = OfflineRegistrationRepository(dao)

    @Provides
    @Singleton
    fun providePlanningRepository(dao: ParcelDao): PlanningRepository = OfflinePlanningRepository(dao)

    @Provides
    @Singleton
    fun provideMunicipalRepository(dao: ParcelDao): MunicipalRepository = OfflineMunicipalRepository(dao)

    @Provides
    @Singleton
    fun provideSupabaseClient(): io.github.jan.supabase.SupabaseClient {
        return com.govtech.landstack.data.remote.SupabaseConfig.client
    }
}
