package com.rudra.georanker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.georanker.domain.ranking.RankedPlace
import com.rudra.georanker.domain.usecase.GetRankedPlacesUseCase
import com.rudra.georanker.domain.model.Filters
import com.rudra.georanker.domain.ranking.AreaInsight
import com.rudra.georanker.sync.SyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlaceViewModel(
    private val getRankedPlacesUseCase: GetRankedPlacesUseCase,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _filters = MutableStateFlow(Filters())
    val filters: StateFlow<Filters> = _filters.asStateFlow()

    val isSyncing = syncManager.isSyncing

    // Mock user location (NYC area to match MockDataSource)
    private val mockLat = 40.730610
    private val mockLng = -73.935242

    private val _zoom = MutableStateFlow(13f)
    val zoom: StateFlow<Float> = _zoom.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val rankedPlaces: StateFlow<List<RankedPlace>> = _filters
        .flatMapLatest { currentFilters ->
            getRankedPlacesUseCase.execute(mockLat, mockLng, currentFilters)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val areaInsight: StateFlow<AreaInsight> = combine(rankedPlaces, _zoom) { places, zoom ->
        AreaInsight.fromPlaces(places, zoom)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AreaInsight(0.0, 0, 0, "Analyzing area...", "Local Area", emptyMap())
        )

    fun updateFilters(newFilters: Filters) {
        _filters.value = newFilters
    }

    fun updateZoom(newZoom: Float) {
        _zoom.value = newZoom
    }

    fun refresh() {
        viewModelScope.launch {
            syncManager.sync()
        }
    }
}
