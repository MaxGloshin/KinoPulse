package ru.openstream.kinopulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.openstream.kinopulse.ui.CatalogViewModel
import ru.openstream.kinopulse.ui.screens.DetailsScreen
import ru.openstream.kinopulse.ui.screens.HomeScreen
import ru.openstream.kinopulse.ui.screens.VideoPlayerScreen
import ru.openstream.kinopulse.ui.theme.KinoPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KinoPulseTheme {
                val viewModel: CatalogViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                var selectedMediaId by rememberSaveable { mutableStateOf<String?>(null) }

                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    val activeVariant = uiState.activePlaybackVariant
                    if (activeVariant != null) {
                        val savedPos = uiState.watchProgress[viewModel.playbackKey(uiState.activePlaybackMediaId, activeVariant)]?.positionMs ?: 0L
                        VideoPlayerScreen(
                            title = uiState.activePlaybackTitle,
                            variant = activeVariant,
                            initialPositionMs = savedPos,
                            onSavePosition = { pos, finished -> viewModel.savePosition(pos, completed = finished) },
                            onCloseWithPosition = { pos, finished ->
                                viewModel.savePosition(pos, close = true, completed = finished)
                            }
                        )
                    } else {
                        val currentMedia = selectedMediaId?.let { viewModel.getMediaById(it) }
                        if (currentMedia != null) {
                            BackHandler {
                                selectedMediaId = null
                            }
                            DetailsScreen(
                                item = currentMedia,
                                isFavorite = uiState.favorites.contains(currentMedia.id),
                                savedPositionMs = uiState.watchProgress[currentMedia.id]?.positionMs,
                                onBack = { selectedMediaId = null },
                                onToggleFavorite = { viewModel.toggleFavorite(currentMedia.id) },
                                onPlayVariant = { title, variant ->
                                    viewModel.startPlayback(currentMedia.id, title, variant)
                                }
                            )
                        } else {
                            HomeScreen(
                                state = uiState,
                                onSearchQueryChange = viewModel::onSearchQueryChange,
                                onFilterChange = viewModel::onFilterChange,
                                onFavoritesFilter = viewModel::toggleFavoritesFilter,
                                onResume = viewModel::resume,
                                onMediaClick = { id -> selectedMediaId = id }
                            )
                        }
                    }
                }
            }
        }
    }
}
