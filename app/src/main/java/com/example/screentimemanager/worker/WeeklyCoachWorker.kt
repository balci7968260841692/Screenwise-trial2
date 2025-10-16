package com.example.screentimemanager.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeeklyCoachWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val repository = CoachWorkerHelper.createRepository(applicationContext)
        return@withContext try {
            repository.fetchCoachTips(emptyMap())
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}
