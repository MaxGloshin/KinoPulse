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
import ru.openstream.kinopulse.data.open.OpenMoviesCatalogAdapter
import ru.openstream.kinopulse.domain.source.DirectVideoLinks
import ru.openstream.kinopulse.domain.model.playbackKey
import ru.openstream.kinopulse.domain.model.MediaItem
import ru.openstream.kinopulse.domain.model.MediaType
import ru.openstream.kinopulse.domain.model.StreamVariant
import ru.openstream.kinopulse.domain.source.CatalogSourceAdapter

private val Context.catalogStore by preferencesDataStore(name = "catalog")

data class WatchProgressEntry(
    val mediaId: String,
    val variantId: String,
    val title: String,
    val subtitle: String,
    val positionMs: Long
)

data class UserVideoLink(val title: String, val url: String)

data class CatalogUiState(
    val onlyFavorites: Boolean = false,
    val userLinks: List<UserVideoLink> = emptyList(),
    val linkError: String? = null,
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
    private val adapters: List<CatalogSourceAdapter> = listOf(LocalDemoCatalogAdapter(), OpenMoviesCatalogAdapter())
    private val store = application.catalogStore
    private val stateKey = stringPreferencesKey("state")

    private val _uiState = MutableStateFlow(
        CatalogUiState(
            isDemoMode = adapters.all { it.isDemoSource },
            sourceName = adapters.joinToString(" + ") { it.displayName }
        )
    )
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            val items = adapters.flatMap { adapter -> adapter.getRecentFeed() }
            val stored = store.data.catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }.first()[stateKey]
            val json = runCatching { JSONObject(stored ?: "{}") }.getOrDefault(JSONObject())
            val favoritesJson = json.optJSONArray("favorites") ?: JSONArray()
            val favorites = (0 until favoritesJson.length()).map { favoritesJson.optString(it) }.toSet()
            val linksJson = json.optJSONArray("links") ?: JSONArray()
            val links = (0 until linksJson.length()).mapNotNull { index ->
                val obj = linksJson.optJSONObject(index) ?: return@mapNotNull null
                val url = DirectVideoLinks.normalize(obj.optString("url")) ?: return@mapNotNull null
                UserVideoLink(obj.optString("title").trim().take(120).ifEmpty { "Моя видеоссылка" }, url)
            }.distinctBy { it.url }
            val allItems = items + links.map(::makeLinkItem)
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
                    userLinks = links,
                    allItems = allItems,
                    filteredItems = filterItems(allItems, state.searchQuery, state.selectedFilter)
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
        val links = JSONArray()
        state.userLinks.forEach { link ->
            links.put(JSONObject().put("title", link.title).put("url", link.url))
        }
        val json = JSONObject().put("favorites", JSONArray(state.favorites.toList()))
            .put("progress", progress).put("links", links).toString()
        viewModelScope.launch { store.edit { it[stateKey] = json } }
    }

    /** Adds only an explicitly supplied HTTPS video URL; no scraping or bypassing access controls. */
    fun addDirectLink(title: String, url: String): Boolean {
        val normalized = DirectVideoLinks.normalize(url)
        if (normalized == null) {
            _uiState.update { it.copy(linkError = "Нужна корректная прямая HTTPS-ссылка на видео.") }
            return false
        }
        val link = UserVideoLink(title.trim().take(120).ifEmpty { "Моё видео" }, normalized)
        _uiState.update { state ->
            val links = state.userLinks.filterNot { it.url == normalized } + link
            val merged = state.allItems.filterNot { it.id.startsWith("user_link_") } +
                links.map(::makeLinkItem)
            state.copy(
                userLinks = links,
                linkError = null,
                allItems = merged,
                filteredItems = filterItems(merged, state.searchQuery, state.selectedFilter)
            )
        }
        persist()
        return true
    }

    fun removeDirectLink(mediaId: String) {
        _uiState.update { state ->
            val remaining = state.userLinks.filterNot { DirectVideoLinks.stableId(it.url) == mediaId }
            val items = state.allItems.filterNot { it.id == mediaId }
            state.copy(
                userLinks = remaining,
                favorites = state.favorites - mediaId,
                watchProgress = state.watchProgress.filterKeys { it != mediaId },
                allItems = items,
                filteredItems = filterItems(items, state.searchQuery, state.selectedFilter)
            )
        }
        persist()
    }

    private fun makeLinkItem(link: UserVideoLink): MediaItem = MediaItem(
        id = DirectVideoLinks.stableId(link.url),
        title = link.title,
        originalTitle = link.title,
        year = 0,
        type = MediaType.MOVIE,
        rating = "—",
        genres = listOf("Мои ссылки"),
        description = "Добавлено вручную. KinoPulse не проверяет право доступа к содержимому и не обходит защиту.",
        licenseInfo = "Пользовательский источник; права определяются владельцем видео.",
        posterColorStart = 0xFF163B45,
        posterColorEnd = 0xFF091A25,
        posterAccentColor = 0xFF47D7C1,
        directVariants = listOf(StreamVariant(
            id = "direct",
            sourceName = "Моя HTTPS-ссылка",
            audioTrack = "Источник",
            quality = "Авто",
            streamUrl = link.url
        )),
        isDemoContent = false
    )

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

    fun savePosition(positionMs: Long, close: Boolean = false) {
        _uiState.update { state ->
            val mediaId = state.activePlaybackMediaId
            val updatedProgress = state.watchProgress.toMutableMap()
            val variant = state.activePlaybackVariant
            if (mediaId.isNotEmpty() && variant != null) {
                updatedProgress[playbackKey(mediaId, variant)] = WatchProgressEntry(
                    mediaId = mediaId,
                    variantId = variant.id,
                    title = state.activePlaybackTitle,
                    subtitle = state.activePlaybackVariant?.let { "${it.quality} • ${it.audioTrack}" } ?: "",
                    positionMs = positionMs.coerceAtLeast(0)
                )
            }
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
