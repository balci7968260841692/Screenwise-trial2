package com.example.screentimemanager.di

import android.content.Context
import androidx.room.Room
import com.example.screentimemanager.BuildConfig
import com.example.screentimemanager.data.local.db.ScreenTimeDatabase
import com.example.screentimemanager.data.preferences.UserPreferences
import com.example.screentimemanager.data.remote.SupabaseFunctionClient
import com.example.screentimemanager.data.repository.CoachRepository
import com.example.screentimemanager.data.repository.LimitsRepository
import com.example.screentimemanager.data.repository.OverridesRepository
import com.example.screentimemanager.data.repository.TrustRepository
import com.example.screentimemanager.data.repository.UsageRepository
import com.example.screentimemanager.domain.logic.OverrideNegotiator
import com.example.screentimemanager.domain.logic.TrustCalculator
import com.example.screentimemanager.domain.logic.UsageScheduleEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.GoTrue
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScreenTimeDatabase =
        Room.databaseBuilder(context, ScreenTimeDatabase::class.java, "screen_time.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAppLimitDao(db: ScreenTimeDatabase) = db.appLimitDao()

    @Provides
    fun provideUsageSampleDao(db: ScreenTimeDatabase) = db.usageSampleDao()

    @Provides
    fun provideOverrideRequestDao(db: ScreenTimeDatabase) = db.overrideRequestDao()

    @Provides
    fun provideTrustDao(db: ScreenTimeDatabase) = db.trustStateDao()

    @Provides
    fun provideFutureMeDao(db: ScreenTimeDatabase) = db.futureMeNoteDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context) = UserPreferences(context)

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Functions)
        install(Postgrest)
        install(GoTrue)
    }

    @Provides
    @Singleton
    fun provideFunctionClient(client: SupabaseClient) = SupabaseFunctionClient(client)

    @Provides
    @Singleton
    fun provideTrustCalculator() = TrustCalculator()

    @Provides
    @Singleton
    fun provideUsageScheduleEngine() = UsageScheduleEngine()

    @Provides
    @Singleton
    fun provideOverrideNegotiator(trustCalculator: TrustCalculator) = OverrideNegotiator(trustCalculator)

    @Provides
    @Singleton
    fun provideLimitsRepository(db: ScreenTimeDatabase) = LimitsRepository(db.appLimitDao())

    @Provides
    @Singleton
    fun provideUsageRepository(db: ScreenTimeDatabase) = UsageRepository(db.usageSampleDao())

    @Provides
    @Singleton
    fun provideOverridesRepository(
        db: ScreenTimeDatabase,
        trustCalculator: TrustCalculator,
        functionClient: SupabaseFunctionClient
    ) = OverridesRepository(
        overrideDao = db.overrideRequestDao(),
        trustDao = db.trustStateDao(),
        trustCalculator = trustCalculator,
        functionClient = functionClient
    )

    @Provides
    @Singleton
    fun provideTrustRepository(db: ScreenTimeDatabase, trustCalculator: TrustCalculator) =
        TrustRepository(db.trustStateDao(), trustCalculator)

    @Provides
    @Singleton
    fun provideCoachRepository(
        db: ScreenTimeDatabase,
        functionClient: SupabaseFunctionClient
    ) = CoachRepository(db.futureMeNoteDao(), functionClient)
}
