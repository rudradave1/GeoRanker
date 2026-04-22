package com.rudra.georanker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.georanker.domain.ranking.AreaInsight
import com.rudra.georanker.domain.ranking.RankedPlace
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankedPlaceList(
    rankedPlaces: List<RankedPlace>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onPlaceClick: (RankedPlace) -> Unit = {}
) {
    val areaInsight = remember(rankedPlaces) { AreaInsight.fromPlaces(rankedPlaces) }

    Box(modifier = modifier) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AreaInsightCard(
                        insight = areaInsight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                itemsIndexed(rankedPlaces, key = { _, item -> item.place.id }) { index, rankedPlace ->
                    PlaceItem(
                        rankedPlace = rankedPlace,
                        rank = index + 1,
                        onClick = { onPlaceClick(rankedPlace) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceItem(
    rankedPlace: RankedPlace,
    rank: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Title Row: [Name] [#Rank • Score]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rankedPlace.place.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                RankScoreChip(rank = rank, score = rankedPlace.score)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Row: ⭐ 4.8 • $$ • distance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⭐ ${rankedPlace.place.rating}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "$".repeat(rankedPlace.place.priceLevel),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                rankedPlace.distanceKm?.let { distance ->
                    Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    val distanceText = if (distance < 1.0) {
                        "${(distance * 1000).roundToInt()} m away"
                    } else {
                        // Manual format for KMP (String.format is not available in commonMain)
                        val rounded = (distance * 10).roundToInt() / 10.0
                        "$rounded km away"
                    }
                    
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Insight Text (no nested box)
            Text(
                text = rankedPlace.insight,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RankScoreChip(rank: Int, score: Double) {
    val containerColor = when {
        rank == 1 -> Color(0xFFE8F5E9) // Green 50
        rank <= 3 -> Color(0xFFFFF3E0) // Orange 50
        else -> Color(0xFFF5F5F5)      // Grey 100
    }
    
    val contentColor = when {
        rank == 1 -> Color(0xFF2E7D32) // Green 800
        rank <= 3 -> Color(0xFFE65100) // Orange 900
        else -> Color(0xFF616161)      // Grey 700
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        // Manual format for KMP
        val formattedScore = (score * 10).roundToInt() / 10.0
        Text(
            text = "#$rank • $formattedScore",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
