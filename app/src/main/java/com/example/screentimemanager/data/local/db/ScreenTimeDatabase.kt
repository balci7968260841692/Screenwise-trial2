package com.example.screentimemanager.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.screentimemanager.data.local.dao.AppLimitDao
import com.example.screentimemanager.data.local.dao.FutureMeNoteDao
import com.example.screentimemanager.data.local.dao.OverrideRequestDao
import com.example.screentimemanager.data.local.dao.TrustStateDao
import com.example.screentimemanager.data.local.dao.UsageSampleDao
import com.example.screentimemanager.data.local.entity.AppLimitEntity
import com.example.screentimemanager.data.local.entity.FutureMeNoteEntity
import com.example.screentimemanager.data.local.entity.OverrideRequestEntity
import com.example.screentimemanager.data.local.entity.TrustStateEntity
import com.example.screentimemanager.data.local.entity.UsageSampleEntity

@Database(
    entities = [
        AppLimitEntity::class,
        UsageSampleEntity::class,
        OverrideRequestEntity::class,
        TrustStateEntity::class,
        FutureMeNoteEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ScreenTimeDatabase : RoomDatabase() {
    abstract fun appLimitDao(): AppLimitDao
    abstract fun usageSampleDao(): UsageSampleDao
    abstract fun overrideRequestDao(): OverrideRequestDao
    abstract fun trustStateDao(): TrustStateDao
    abstract fun futureMeNoteDao(): FutureMeNoteDao
}
