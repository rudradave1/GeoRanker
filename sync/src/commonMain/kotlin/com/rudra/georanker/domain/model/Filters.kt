package com.rudra.georanker.domain.model

data class Filters(
    val minRating: Double = 0.0,
    val maxPriceLevel: Int = 4,
    val selectedCategory: PlaceCategory = PlaceCategory.CAFE
)
