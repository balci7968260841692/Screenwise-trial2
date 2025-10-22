package com.example.screentimemanager.worker

import android.content.Context
import androidx.room.Room
import com.example.screentimemanager.BuildConfig
import com.example.screentimemanager.data.local.db.ScreenTimeDatabase
import com.example.screentimemanager.data.remote.SupabaseFunctionClient
import com.example.screentimemanager.data.repository.CoachRepository
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions

object CoachWorkerHelper {
    fun createRepository(context: Context): CoachRepository {
        val db = Room.databaseBuilder(context, ScreenTimeDatabase::class.java, "screen_time.db")
            .fallbackToDestructiveMigration()
            .build()
        val client = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) {
            install(Functions)
        }
        val functionClient = SupabaseFunctionClient(client)
        return CoachRepository(db.futureMeNoteDao(), functionClient)
    }
}
