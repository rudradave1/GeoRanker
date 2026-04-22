package com.rudra.georanker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rudra.georanker.data.PlaceRepository
import com.rudra.georanker.data.RemoteDataSource
import com.rudra.georanker.domain.model.Place

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: PlaceRepository,
    private val remoteDataSource: RemoteDataSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Fetch remote data
            val remotePlaces = remoteDataSource.fetchPlaces()
            
            // 2. Fetch local data for comparison
            val localPlacesMap = repository.getAllPlaces().associateBy { it.id }
            
            // 3. Conflict Resolution Logic:
            // if remote.updatedAt > local.updatedAt -> update
            // else -> ignore
            val placesToUpsert = remotePlaces.filter { remote ->
                val local = localPlacesMap[remote.id]
                local == null || remote.updatedAt > local.updatedAt
            }

            // 4. Update DB only with necessary changes
            if (placesToUpsert.isNotEmpty()) {
                repository.upsertPlaces(placesToUpsert)
            }

            Result.success()
        } catch (e: Exception) {
            // Retry on transient failures (network issues, etc.)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
