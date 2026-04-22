package com.rudra.georanker

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rudra.georanker.data.MockDataSource
import com.rudra.georanker.data.FakeRemoteDataSource
import com.rudra.georanker.data.PlaceRepository
import com.rudra.georanker.db.AppDatabase
import com.rudra.georanker.worker.SyncWorker
import com.rudra.georanker.sync.SyncManager
import com.rudra.georanker.db.DatabaseDriverFactory
import java.util.concurrent.TimeUnit

class GeoRankerApplication : Application(), Configuration.Provider {

    lateinit var repository: PlaceRepository
    lateinit var remoteDataSource: FakeRemoteDataSource
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        
        // Manual DI for simplicity in this example
        val driver = DatabaseDriverFactory(this).createDriver()
        val database = AppDatabase(driver)
        repository = PlaceRepository(database)
        remoteDataSource = FakeRemoteDataSource(MockDataSource())
        syncManager = SyncManager(repository, remoteDataSource)

        // Clear old hardcoded data to make room for the 200+ item dataset
        database.placeQueries.deleteAllPlaces()

        scheduleSync()
        triggerImmediateSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: android.content.Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): androidx.work.ListenableWorker? {
                    return when (workerClassName) {
                        SyncWorker::class.java.name -> SyncWorker(
                            appContext,
                            workerParameters,
                            repository,
                            remoteDataSource
                        )
                        else -> null
                    }
                }
            })
            .build()

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PlaceSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "OneTimePlaceSync",
            ExistingWorkPolicy.REPLACE,
            oneTimeSyncRequest
        )
    }
}
