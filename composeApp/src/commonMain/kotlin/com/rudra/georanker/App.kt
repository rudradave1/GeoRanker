package com.rudra.georanker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.georanker.data.PlaceRepository
import com.rudra.georanker.domain.ranking.RankedPlace
import com.rudra.georanker.domain.usecase.GetRankedPlacesUseCase
import com.rudra.georanker.ui.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    repository: PlaceRepository,
    useCase: GetRankedPlacesUseCase,
    syncManager: com.rudra.georanker.sync.SyncManager,
    mapScreen: @Composable (rankedPlaces: List<RankedPlace>, padding: PaddingValues, onZoomChange: (Float) -> Unit, onToggleView: () -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val viewModel: PlaceViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                PlaceViewModel(useCase, syncManager)
            }
        }
    )
    
    val isSyncing by viewModel.isSyncing.collectAsState()
    val rankedPlaces by viewModel.rankedPlaces.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val areaInsight by viewModel.areaInsight.collectAsState()
    var showMap by remember { mutableStateOf(true) }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Layer: Map or List
            if (showMap) {
                mapScreen(
                    rankedPlaces,
                    PaddingValues(0.dp),
                    { viewModel.updateZoom(it) }
                ) {
                    showMap = false
                }
            } else {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("GeoRanker", style = MaterialTheme.typography.titleMedium) },
                            actions = {
                                IconButton(onClick = { showMap = true }) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Show Map")
                                }
                            }
                        )
                    }
                ) { padding ->
                    RankedPlaceList(
                        rankedPlaces = rankedPlaces,
                        isRefreshing = isSyncing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.padding(padding).fillMaxSize(),
                        onPlaceClick = { /* Scroll to or highlight in list */ }
                    )
                }
            }

            // Overlay Layer (Only on Map)
            if (showMap) {
                MapOverlayUI(
                    title = "GeoRanker",
                    onToggleView = { showMap = false },
                    isListView = false,
                    filters = filters,
                    onFilterChange = { viewModel.updateFilters(it) },
                    areaInsight = areaInsight,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
