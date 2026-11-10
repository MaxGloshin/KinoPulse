package ru.openstream.kinopulse.domain.source

import ru.openstream.kinopulse.domain.model.MediaItem

enum class SourceCapability {
    RECENT_RELEASES,
    SEARCH,
    DETAILS,
    SEASONS_AND_EPISODES,
    DIRECT_STREAM_VARIANTS
}

/**
 * Единый интерфейс адаптеров источников.
 * Позволяет подключать новые провайдеры без изменения UI-слоя.
 */
interface CatalogSourceAdapter {
    val sourceId: String
    val displayName: String
    val isDemoSource: Boolean
    val capabilities: Set<SourceCapability>

    suspend fun getRecentFeed(): List<MediaItem>
    suspend fun search(query: String): List<MediaItem>
    suspend fun getDetails(mediaId: String): MediaItem?
}
