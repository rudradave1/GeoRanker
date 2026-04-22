package com.rudra.georanker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.rudra.georanker.domain.ranking.RankingEngine
import com.rudra.georanker.domain.usecase.GetRankedPlacesUseCase
import com.rudra.georanker.ui.MapScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = application as GeoRankerApplication
        val repository = app.repository

        setContent {
            val useCase = remember {
                GetRankedPlacesUseCase(repository, RankingEngine())
            }

            App(
                repository = repository,
                useCase = useCase,
                syncManager = app.syncManager,
                mapScreen = { rankedPlaces, padding, onZoomChange, onToggle ->
                    MapScreen(
                        rankedPlaces = rankedPlaces,
                        onZoomChange = onZoomChange,
                        onPlaceClick = { /* Handle click */ },
                        onToggleView = onToggle
                    )
                }
            )
        }
    }
}
