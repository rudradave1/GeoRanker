package com.rudra.georanker.sync

import com.rudra.georanker.data.PlaceRepository
import com.rudra.georanker.data.RemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncManager(
    private val repository: PlaceRepository,
    private val remoteDataSource: RemoteDataSource
) {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    suspend fun sync() {
        if (_isSyncing.value) return
        
        _isSyncing.value = true
        try {
            val remotePlaces = remoteDataSource.fetchPlaces()
            repository.upsertPlaces(remotePlaces)
        } catch (e: Exception) {
            // Log error or handle offline state
            e.printStackTrace()
        } finally {
            _isSyncing.value = false
        }
    }
}
