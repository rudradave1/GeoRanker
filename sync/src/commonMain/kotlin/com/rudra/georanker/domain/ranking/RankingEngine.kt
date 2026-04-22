package com.rudra.georanker.domain.ranking

import com.rudra.georanker.domain.model.Place
import kotlin.math.*

data class RankedPlace(
    val place: Place,
    val score: Double,
    val insight: String,
    val distanceKm: Double? = null
)

class RankingEngine {
    fun rank(places: List<Place>, userLat: Double? = null, userLng: Double? = null): List<RankedPlace> {
        val rankedWithDistance = places.map { place ->
            val distance = if (userLat != null && userLng != null) {
                calculateDistance(userLat, userLng, place.latitude, place.longitude)
            } else null
            place to distance
        }

        val maxDistance = rankedWithDistance.mapNotNull { it.second }.maxOrNull() ?: 1.0

        return rankedWithDistance.map { (place, distance) ->
            val score = calculateScore(place, distance, maxDistance)
            val insight = generateInsight(place, distance, rankedWithDistance.mapNotNull { it.second })
            RankedPlace(place, score, insight, distance)
        }.sortedByDescending { it.score }
    }

    private fun calculateScore(place: Place, distance: Double?, maxDistance: Double): Double {
        // Normalize components to 0.0 - 1.0 range
        val ratingNormalized = place.rating / 5.0
        
        // Price: Lower price is better (priceLevel 1-4)
        val priceNormalized = (5.0 - place.placeLevel.coerceIn(1, 4)) / 4.0
        
        // Distance: Smaller distance is better
        val distanceNormalized = if (distance != null) {
            (1.0 - (distance / maxDistance)).coerceIn(0.0, 1.0)
        } else 0.5

        // Weighted sum: 50% rating, 20% price, 30% distance
        return (ratingNormalized * 0.5) + (priceNormalized * 0.2) + (distanceNormalized * 0.3)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth radius in km
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a = sin(dLat / 2).pow(2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun Double.toRadians() = this * PI / 180.0

    private fun generateInsight(place: Place, distance: Double?, allDistances: List<Double>): String {
        val distanceInfo = if (distance != null && allDistances.isNotEmpty()) {
            val closerCount = allDistances.count { it < distance }
            val percentile = 100 - (closerCount.toDouble() / allDistances.size * 100).toInt()
            "closer than $percentile% nearby cafes"
        } else "conveniently located"

        return when {
            place.rating >= 4.5 -> "Highly rated (${place.rating}★) and $distanceInfo"
            place.priceLevel == 1 -> "Budget friendly and $distanceInfo"
            else -> "Popular choice and $distanceInfo"
        }
    }
}

// Extension to handle different property names if they exist in Place model
private val Place.placeLevel: Int get() = try { this.javaClass.getDeclaredField("priceLevel").let { it.isAccessible = true; it.get(this) as Int } } catch(e: Exception) { 2 }
