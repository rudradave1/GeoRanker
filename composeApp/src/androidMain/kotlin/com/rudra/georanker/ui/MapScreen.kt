package com.rudra.georanker.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.Clustering
import com.rudra.georanker.domain.ranking.AreaInsight
import com.rudra.georanker.domain.ranking.RankedPlace
import kotlinx.coroutines.launch

// Wrapper for Clustering
data class PlaceClusterItem(
    val rankedPlace: RankedPlace,
    val itemTitle: String,
    val itemSnippet: String,
    val itemPosition: LatLng
) : ClusterItem {
    override fun getPosition(): LatLng = itemPosition
    override fun getTitle(): String = itemTitle
    override fun getSnippet(): String = itemSnippet
    override fun getZIndex(): Float = 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    rankedPlaces: List<RankedPlace>,
    modifier: Modifier = Modifier,
    onZoomChange: (Float) -> Unit = {},
    onToggleView: () -> Unit = {},
    onPlaceClick: (RankedPlace) -> Unit = {}
) {
    var selectedPlace by remember { mutableStateOf<RankedPlace?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val initialLocation = LatLng(40.730610, -73.935242) // NYC Mock
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 13f)
    }

    // Report zoom changes back to ViewModel
    LaunchedEffect(cameraPositionState.position.zoom) {
        onZoomChange(cameraPositionState.position.zoom)
    }

    // Zoom-based filtering: Show fewer markers when zoomed out
    val visiblePlaces = remember(rankedPlaces, cameraPositionState.position.zoom) {
        val zoom = cameraPositionState.position.zoom
        when {
            zoom < 12f -> rankedPlaces.take(20)
            zoom < 14f -> rankedPlaces.take(60)
            else -> rankedPlaces
        }
    }

    val clusterItems = remember(visiblePlaces) {
        visiblePlaces.map { rp ->
            PlaceClusterItem(
                rankedPlace = rp,
                itemTitle = rp.place.name,
                itemSnippet = "${rp.place.rating} ★ • ${"$".repeat(rp.place.priceLevel)}",
                itemPosition = LatLng(rp.place.latitude, rp.place.longitude)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapClick = {
                selectedPlace = null
                showBottomSheet = false
            }
        ) {
            Clustering(
                items = clusterItems,
                onClusterItemClick = { item ->
                    selectedPlace = item.rankedPlace
                    showBottomSheet = true
                    onPlaceClick(item.rankedPlace)
                    
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(
                                item.position,
                                16f
                            )
                        )
                    }
                    false
                }
            )
        }

        // View Toggle FAB removed as it is now in MapOverlayUI

        if (showBottomSheet && selectedPlace != null) {
            val selectedIndex = rankedPlaces.indexOfFirst { it.place.id == selectedPlace?.place?.id }
            
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false
                    selectedPlace = null
                },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                scrimColor = Color.Black.copy(alpha = 0.32f)
            ) {
                PlaceDetailsCard(
                    rankedPlace = selectedPlace!!,
                    rank = selectedIndex + 1
                )
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
