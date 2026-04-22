package com.rudra.georanker.data

import com.rudra.georanker.domain.model.Place
import kotlinx.coroutines.delay
import kotlin.random.Random

interface RemoteDataSource {
    suspend fun fetchPlaces(): List<Place>
}

class FakeRemoteDataSource(private val mockDataSource: MockDataSource) : RemoteDataSource {
    
    override suspend fun fetchPlaces(): List<Place> {
        // 1. Simulate network delay (500ms - 2000ms)
        delay(Random.nextLong(500, 2000))

        // Request 300 items from mock data source
        return mockDataSource.getPlaces(300)
    }
}
