package ru.openstream.kinopulse.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.openstream.kinopulse.data.demo.LocalDemoCatalogAdapter
import ru.openstream.kinopulse.domain.model.playbackKey
import ru.openstream.kinopulse.domain.model.MediaItem
import ru.openstream.kinopulse.domain.model.MediaType
import ru.openstream.kinopulse.domain.model.StreamVariant
import ru.openstream.kinopulse.domain.source.CatalogSourceAdapter

private val Context.catalogStore by preferencesDataStore(name = "catalog")

data class CatalogUiState(
    val onlyFavorites: Boolean = false,
    val isDemoMode: Boolean = true,
    val sourceName: String = "",
    val allItems: List<MediaItem> = emptyList(),
    val filteredItems: List<MediaItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: MediaType? = null,
    val favorites: Set<String> = emptySet(),
    val watchProgress: Map<String, WatchProgressEntry> = emptyMap(),
    val activePlaybackVariant: StreamVariant? = null,
    val activePlaybackTitle: String = "",
    val activePlaybackMediaId: String = ""
)

class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val adapter: CatalogSourceAdapter = LocalDemoCatalogAdapter()
    private val store = application.catalogStore
    private val stateKey = stringPreferencesKey("state")

    private val _uiState = MutableStateFlow(
        CatalogUiState(
            isDemoMode = adapter.isDemoSource,
            sourceName = adapter.displayName
        )
    )
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            val items = adapter.getRecentFeed()
            val stored = store.data.catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }.first()[stateKey]
            val json = runCatching { JSONObject(stored ?: "{}") }.getOrDefault(JSONObject())
            val favoritesJson = json.optJSONArray("favorites") ?: JSONArray()
            val favorites = (0 until favoritesJson.length()).map { favoritesJson.optString(it) }.toSet()
            val progressJson = json.optJSONObject("progress") ?: JSONObject()
            val progress = mutableMapOf<String, WatchProgressEntry>()
            for (key in progressJson.keys()) {
                val entry = progressJson.optJSONObject(key) ?: continue
                progress[key] = WatchProgressEntry(entry.optString("media"), entry.optString("variant"),
                    entry.optString("title"), entry.optString("subtitle"), entry.optLong("position"))
            }
            _uiState.update { state ->
                state.copy(
                    favorites = favorites,
                    watchProgress = progress,
                    allItems = items,
                    filteredItems = filterItems(items, state.searchQuery, state.selectedFilter)
                )
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = newQuery,
                filteredItems = filterItems(state.allItems, newQuery, state.selectedFilter)
            )
        }
    }

    fun onFilterChange(filter: MediaType?) {
        _uiState.update { state ->
            val nextFilter = if (state.selectedFilter == filter) null else filter
            state.copy(
                selectedFilter = nextFilter,
                filteredItems = filterItems(state.allItems, state.searchQuery, nextFilter)
            )
        }
    }

    fun toggleFavorite(mediaId: String) {
        _uiState.update { state ->
            val next = state.favorites.toMutableSet()
            if (!next.add(mediaId)) {
                next.remove(mediaId)
            }
            state.copy(favorites = next)
        }
        persist()
    }

    fun toggleFavoritesFilter() {
        _uiState.update { it.copy(onlyFavorites = !it.onlyFavorites) }
    }

    private fun persist() {
        val state = _uiState.value
        val progress = JSONObject()
        state.watchProgress.forEach { (key, entry) ->
            progress.put(key, JSONObject().put("media", entry.mediaId).put("variant", entry.variantId)
                .put("title", entry.title).put("subtitle", entry.subtitle).put("position", entry.positionMs))
        }
        val json = JSONObject().put("favorites", JSONArray(state.favorites.toList())).put("progress", progress).toString()
        viewModelScope.launch { store.edit { it[stateKey] = json } }
    }

    fun playbackKey(mediaId: String, variant: StreamVariant): String {
        return getMediaById(mediaId)?.playbackKey(variant) ?: mediaId
    }

    fun resume(entry: WatchProgressEntry) {
        val item = getMediaById(entry.mediaId) ?: return
        val variants = item.directVariants + item.seasons.flatMap { it.episodes }.flatMap { it.variants }
        val variant = variants.find { it.id == entry.variantId } ?: return
        startPlayback(item.id, entry.title, variant)
    }

    fun startPlayback(mediaId: String, title: String, variant: StreamVariant) {
        _uiState.update { state ->
            state.copy(
                activePlaybackMediaId = mediaId,
                activePlaybackTitle = title,
                activePlaybackVariant = variant
            )
        }
    }

    fun savePosition(positionMs: Long, close: Boolean = false, completed: Boolean = false) {
        _uiState.update { state ->
            val mediaId = state.activePlaybackMediaId
            val variant = state.activePlaybackVariant
            val updatedProgress = if (mediaId.isNotEmpty() && variant != null) {
                val entry = if (completed) null else WatchProgressEntry(
                    mediaId = mediaId,
                    variantId = variant.id,
                    title = state.activePlaybackTitle,
                    subtitle = "${variant.quality} • ${variant.audioTrack}",
                    positionMs = positionMs.coerceAtLeast(0)
                )
                updateWatchProgress(state.watchProgress, playbackKey(mediaId, variant), entry)
            } else state.watchProgress
            state.copy(
                watchProgress = updatedProgress,
                activePlaybackVariant = if (close) null else state.activePlaybackVariant,
                activePlaybackTitle = if (close) "" else state.activePlaybackTitle,
                activePlaybackMediaId = if (close) "" else state.activePlaybackMediaId
            )
        }
        persist()
    }

    fun getMediaById(mediaId: String): MediaItem? {
        return _uiState.value.allItems.find { it.id == mediaId }
    }

    private fun filterItems(
        items: List<MediaItem>,
        query: String,
        typeFilter: MediaType?
    ): List<MediaItem> {
        val clean = query.trim().lowercase()
        return items.filter { item ->
            val matchesType = typeFilter == null || item.type == typeFilter
            val matchesQuery = clean.isEmpty() ||
                item.title.lowercase().contains(clean) ||
                item.originalTitle.lowercase().contains(clean) ||
                item.genres.any { it.lowercase().contains(clean) }
            matchesType && matchesQuery
        }
    }
}
