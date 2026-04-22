package com.rudra.georanker.domain.ranking

import com.rudra.georanker.domain.model.Place

data class AreaInsight(
    val averageRating: Double,
    val averagePrice: Int,
    val density: Int,
    val insightText: String,
    val areaName: String,
    val priceDistribution: Map<Int, Int>
) {
    companion object {
        fun fromPlaces(rankedPlaces: List<RankedPlace>, zoom: Float = 15f): AreaInsight {
            if (rankedPlaces.isEmpty()) {
                return AreaInsight(0.0, 0, 0, "No places found in this area.", "Unknown Area", emptyMap())
            }

            val avgRating = rankedPlaces.map { it.place.rating }.average()
            val avgPrice = rankedPlaces.map { it.place.priceLevel }.average().toInt()
            val density = rankedPlaces.size
            
            val priceDist = rankedPlaces.groupBy { it.place.priceLevel }
                .mapValues { it.value.size }

            val highRatedCount = rankedPlaces.count { it.place.rating >= 4.0 }
            val isHighRatedArea = highRatedCount > density * 0.5
            val isPremiumArea = (priceDist[3] ?: 0) + (priceDist[4] ?: 0) > density * 0.5
            val isBudgetArea = (priceDist[1] ?: 0) + (priceDist[2] ?: 0) > density * 0.6

            val areaName = when {
                zoom < 12f -> "City Overview"
                zoom < 14f -> "District View"
                else -> "Local Area"
            }

            val insightPrefix = when {
                zoom < 12f -> "Top cafes in the city"
                zoom < 14f -> "Best cafes in this district"
                else -> "Best cafes within 500m"
            }

            val insightText = when {
                isHighRatedArea && isPremiumArea -> "$insightPrefix: Premium & top-rated"
                isHighRatedArea && isBudgetArea -> "Mostly mid-range cafes with 4.0★+ ratings"
                isHighRatedArea -> "$insightPrefix: High-rated dominant"
                density > 30 -> "High density cafe zone"
                else -> "A diverse range of cafes available"
            }

            return AreaInsight(avgRating, avgPrice, density, insightText, areaName, priceDist)
        }
    }
}
