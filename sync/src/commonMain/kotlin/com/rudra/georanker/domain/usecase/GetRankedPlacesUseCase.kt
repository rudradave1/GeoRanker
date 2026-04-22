package com.rudra.georanker.domain.usecase

import com.rudra.georanker.data.PlaceRepository
import com.rudra.georanker.domain.model.Filters
import com.rudra.georanker.domain.ranking.RankedPlace
import com.rudra.georanker.domain.ranking.RankingEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRankedPlacesUseCase(
    private val repository: PlaceRepository,
    private val rankingEngine: RankingEngine
) {
    fun execute(lat: Double?, lng: Double?, filters: Filters = Filters()): Flow<List<RankedPlace>> {
        return repository.observePlaces()
            .map { places ->
                val filteredPlaces = places.filter { place ->
                    place.category == filters.selectedCategory &&
                    place.rating >= filters.minRating && 
                    place.priceLevel <= filters.maxPriceLevel
                }
                rankingEngine.rank(filteredPlaces, lat, lng)
            }
    }
}
