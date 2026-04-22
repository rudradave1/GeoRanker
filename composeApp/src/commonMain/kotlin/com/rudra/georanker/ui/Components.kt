package com.rudra.georanker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.georanker.domain.model.PlaceCategory
import com.rudra.georanker.domain.ranking.AreaInsight
import com.rudra.georanker.domain.ranking.RankedPlace

@Composable
fun PlaceDetailsCard(
    rankedPlace: RankedPlace,
    rank: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Name and Rank/Score Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rankedPlace.place.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rating and Price
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${rankedPlace.place.rating} ⭐",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "$".repeat(rankedPlace.place.priceLevel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    rankedPlace.distanceKm?.let { distance ->
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${"%.1f".format(distance)} km away",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Surface(
                color = getScoreColor(rankedPlace.score).copy(alpha = 0.1f),
                contentColor = getScoreColor(rankedPlace.score),
                shape = CircleShape,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Insight Section - Simplified
        Text(
            text = "INSIGHT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = rankedPlace.insight,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapOverlayUI(
    title: String,
    onToggleView: () -> Unit,
    isListView: Boolean,
    filters: com.rudra.georanker.domain.model.Filters,
    onFilterChange: (com.rudra.georanker.domain.model.Filters) -> Unit,
    areaInsight: AreaInsight,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // Compact Title Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 0.dp // Embedded feel
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onToggleView,
                        modifier = Modifier.size(32.dp).background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = if (isListView) Icons.Default.LocationOn else Icons.AutoMirrored.Filled.List,
                            contentDescription = "Toggle View",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                CategorySelector(
                    selectedCategory = filters.selectedCategory,
                    onCategorySelected = { onFilterChange(filters.copy(selectedCategory = it)) }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FilterChipsRow(
                filters = filters,
                onFilterChange = onFilterChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            AreaInsightCard(
                insight = areaInsight
            )
        }
    }
}

@Composable
fun CategorySelector(
    selectedCategory: PlaceCategory,
    onCategorySelected: (PlaceCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PlaceCategory.entries) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { 
                    Text(
                        text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = FilterChipDefaults.filterChipElevation(elevation = 0.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    filters: com.rudra.georanker.domain.model.Filters,
    onFilterChange: (com.rudra.georanker.domain.model.Filters) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val chipColors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        )
        val chipBorder = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        FilterChip(
            selected = filters.minRating >= 4.0,
            onClick = {
                onFilterChange(filters.copy(minRating = if (filters.minRating >= 4.0) 0.0 else 4.0))
            },
            label = { Text("4.0+") },
            leadingIcon = { Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp)) },
            colors = chipColors,
            border = chipBorder,
            elevation = FilterChipDefaults.filterChipElevation(elevation = 0.dp),
            shape = RoundedCornerShape(12.dp)
        )

        FilterChip(
            selected = filters.maxPriceLevel <= 2,
            onClick = {
                onFilterChange(filters.copy(maxPriceLevel = if (filters.maxPriceLevel <= 2) 4 else 2))
            },
            label = { Text("$$ Budget") },
            colors = chipColors,
            border = chipBorder,
            elevation = FilterChipDefaults.filterChipElevation(elevation = 0.dp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun AreaInsightCard(
    insight: AreaInsight,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Slightly sharper for compact look
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.areaName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = insight.insightText,
                    style = MaterialTheme.typography.bodySmall, // Reduced from bodyMedium
                    lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    text = "${"%.1f".format(insight.averageRating)} ★",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun getScoreColor(score: Double): Color {
    return when {
        score >= 0.8 -> Color(0xFF2E7D32) // Green 800
        score >= 0.5 -> Color(0xFFEF6C00) // Orange 800
        else -> Color(0xFFC62828) // Red 800
    }
}
