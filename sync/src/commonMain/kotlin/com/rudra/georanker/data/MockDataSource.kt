package com.rudra.georanker.data

import com.rudra.georanker.domain.model.Place
import com.rudra.georanker.domain.model.PlaceCategory
import kotlin.random.Random

class MockDataSource(private val seed: Long? = null) {
    private val random = seed?.let { Random(it) } ?: Random(System.currentTimeMillis())

    private val neighborhoodNames = listOf(
        "Chelsea", "Upper East Side", "Williamsburg", "Astoria", "Greenwich Village",
        "DUMBO", "SoHo", "Financial District", "Harlem", "Long Island City"
    )

    private val templates = mapOf(
        PlaceCategory.CAFE to listOf(
            "The {adj} Bean", "Urban {noun} Roasters", "Daily {noun}", "{adj} Drip Cafe",
            "The {noun} & The {noun}", "{neighborhood} Espresso", "Vintage {noun}", "Little {adj} Cafe"
        ),
        PlaceCategory.RESTAURANT to listOf(
            "The {adj} Table", "{neighborhood} Bistro", "Salt & {noun}", "{adj} Kitchen",
            "Iron {noun} Grill", "The {adj} Fork", "Grand {neighborhood} Eatery", "{noun} House"
        ),
        PlaceCategory.GYM to listOf(
            "Iron {noun} Fitness", "{neighborhood} Strength", "Swift {adj} Gym", "Core {noun} Studio",
            "Prime {adj} Athletics", "The {noun} Box", "Elite {neighborhood} Club", "Zenith {noun}"
        ),
        PlaceCategory.PARK to listOf(
            "{neighborhood} Central Park", "{adj} Meadow Grove", "Sunset {noun} Plaza", "Liberty {neighborhood} Gardens",
            "Riverside {noun} Reserve", "{adj} Hill Park", "The {noun} Common", "{neighborhood} Green"
        ),
        PlaceCategory.HOSPITAL to listOf(
            "St. {noun} Memorial", "{neighborhood} General Hospital", "Mercy {adj} Health", "Unity {noun} Center",
            "City {neighborhood} Wellness", "North {adj} Medical", "Hope {noun} Institute", "Central {neighborhood} Care"
        ),
        PlaceCategory.SCHOOL to listOf(
            "{neighborhood} Academy", "Beacon {adj} High", "{neighborhood} Charter School", "Maple {noun} Prep",
            "Summit {adj} Primary", "River {noun} Institute", "{neighborhood} Junior Academy", "Pine {adj} School"
        ),
        PlaceCategory.COWORKING to listOf(
            "The {neighborhood} Hub", "Think {noun} Space", "Common {adj} Desk", "Logic {noun} Lab",
            "Nexus {neighborhood} Studio", "Open {adj} Collective", "Idea {noun} Station", "The {neighborhood} Floor"
        )
    )

    private val adjectives = listOf("Golden", "Rusty", "Blue", "Urban", "Vintage", "Velvet", "Hidden", "Grand", "Swift", "Silent", "Iron", "Modern")
    private val nouns = listOf("Leaf", "Grind", "Plate", "Bridge", "Tower", "Brook", "Stone", "Anchor", "Compass", "Globe", "Oak", "Wave")

    private val clusters = listOf(
        // Core Manhattan Area
        ClusterInfo(40.7580, -73.9855, 0.04, 250, 3.8..4.9, 2..4, "Manhattan"), 
        // Brooklyn Tech/Trendy Area
        ClusterInfo(40.6782, -73.9442, 0.05, 150, 3.5..4.7, 1..3, "Brooklyn"),
        // Queens Residential/Local Area
        ClusterInfo(40.7282, -73.7949, 0.08, 100, 3.0..4.5, 1..2, "Queens")
    )

    private data class ClusterInfo(
        val lat: Double, val lng: Double, val radius: Double, 
        val count: Int, val ratingRange: ClosedFloatingPointRange<Double>, 
        val priceRange: IntRange, val clusterName: String
    )

    fun getPlaces(totalCount: Int = 500): List<Place> {
        val places = mutableListOf<Place>()
        
        clusters.forEach { cluster ->
            repeat(cluster.count) { i ->
                val angle = random.nextDouble() * 2 * Math.PI
                val r = random.nextDouble() * cluster.radius
                val lat = cluster.lat + r * Math.cos(angle)
                val lng = cluster.lng + r * Math.sin(angle)
                
                places.add(generatePlace(
                    id = "p_${cluster.clusterName}_$i", 
                    lat = lat, 
                    lng = lng, 
                    ratingRange = cluster.ratingRange, 
                    priceRange = cluster.priceRange
                ))
            }
        }

        // Fill remaining with random outliers
        val remaining = totalCount - places.size
        if (remaining > 0) {
            repeat(remaining) { i ->
                val lat = 40.7128 + (random.nextDouble() - 0.5) * 0.4
                val lng = -74.0060 + (random.nextDouble() - 0.5) * 0.4
                places.add(generatePlace("outlier_$i", lat, lng, 2.5..4.5, 1..4))
            }
        }

        return places.shuffled(random)
    }

    private fun generatePlace(
        id: String, 
        lat: Double, 
        lng: Double, 
        ratingRange: ClosedFloatingPointRange<Double>,
        priceRange: IntRange
    ): Place {
        val category = PlaceCategory.entries.random(random)
        val rating = random.nextDouble(ratingRange.start, ratingRange.endInclusive)
        val priceLevel = priceRange.random(random)
        val neighborhood = neighborhoodNames.random(random)
        
        val name = generateName(category, neighborhood)
        
        return Place(
            id = id,
            name = name,
            rating = (rating * 10).toInt() / 10.0,
            priceLevel = priceLevel,
            category = category,
            latitude = lat,
            longitude = lng,
            updatedAt = System.currentTimeMillis() - random.nextLong(0, 86400000 * 7), // within last week
            description = "A top-rated ${category.name.lowercase()} located in the heart of $neighborhood."
        )
    }

    private fun generateName(category: PlaceCategory, neighborhood: String): String {
        val template = templates[category]?.random(random) ?: "Place"
        return template
            .replace("{adj}", adjectives.random(random))
            .replace("{noun}", nouns.random(random))
            .replace("{neighborhood}", neighborhood)
    }
}
