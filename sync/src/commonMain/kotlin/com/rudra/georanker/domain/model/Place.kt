package com.rudra.georanker.domain.model

data class Place(
    val id: String,
    val name: String,
    val rating: Double,
    val priceLevel: Int, // 1-4
    val category: PlaceCategory,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: Long = 0L,
    val description: String = ""
)
