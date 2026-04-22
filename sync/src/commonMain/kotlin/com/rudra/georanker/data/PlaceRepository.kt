package com.rudra.georanker.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rudra.georanker.db.AppDatabase
import com.rudra.georanker.db.PlaceEntity
import com.rudra.georanker.domain.model.Place
import com.rudra.georanker.domain.model.PlaceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaceRepository(db: AppDatabase) {
    private val queries = db.placeQueries

    fun observePlaces(): Flow<List<Place>> {
        return queries.observePlaces()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getAllPlaces(): List<Place> {
        return queries.getAllPlaces().executeAsList().map { it.toDomain() }
    }

    suspend fun upsertPlaces(places: List<Place>) {
        queries.transaction {
            places.forEach { place ->
                queries.insertPlace(
                    id = place.id,
                    name = place.name,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    rating = place.rating,
                    priceLevel = place.priceLevel.toLong(),
                    category = place.category.name,
                    updatedAt = place.updatedAt
                )
            }
        }
    }

    private fun PlaceEntity.toDomain(): Place = Place(
        id = id,
        name = name,
        rating = rating,
        priceLevel = priceLevel.toInt(),
        category = PlaceCategory.valueOf(category),
        latitude = latitude,
        longitude = longitude,
        updatedAt = updatedAt,
        description = "A great place to visit."
    )
}
