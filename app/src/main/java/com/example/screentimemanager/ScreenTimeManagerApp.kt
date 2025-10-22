package com.example.screentimemanager

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.screentimemanager.service.UsageMonitorForegroundService
import com.example.screentimemanager.worker.WeeklyCoachWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class ScreenTimeManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ContextCompat.startForegroundService(this, Intent(this, UsageMonitorForegroundService::class.java))
        scheduleWeeklyCoach()
    }

    private fun scheduleWeeklyCoach() {
        val request = PeriodicWorkRequestBuilder<WeeklyCoachWorker>(7, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weekly_coach",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
