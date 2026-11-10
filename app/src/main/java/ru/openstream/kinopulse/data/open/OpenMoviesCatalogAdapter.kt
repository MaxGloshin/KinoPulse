package ru.openstream.kinopulse.data.open

import ru.openstream.kinopulse.domain.model.MediaItem
import ru.openstream.kinopulse.domain.model.MediaType
import ru.openstream.kinopulse.domain.model.StreamVariant
import ru.openstream.kinopulse.domain.source.CatalogSourceAdapter
import ru.openstream.kinopulse.domain.source.SourceCapability

/**
 * Independent curated catalogue of complete openly licensed films.
 * Video is streamed from the rights holder or Wikimedia Commons, never bundled in our APK.
 */
class OpenMoviesCatalogAdapter : CatalogSourceAdapter {
    override val sourceId = "open_movies"
    override val displayName = "Открытые фильмы"
    override val isDemoSource = false
    override val capabilities = setOf(
        SourceCapability.RECENT_RELEASES,
        SourceCapability.SEARCH,
        SourceCapability.DETAILS,
        SourceCapability.DIRECT_STREAM_VARIANTS
    )

    private val films = listOf(
        MediaItem(
            id = "open_movies_sintel_full",
            title = "Синтел — полный фильм",
            originalTitle = "Sintel",
            year = 2010,
            type = MediaType.MOVIE,
            rating = "—",
            genres = listOf("Анимация", "Фэнтези", "Приключения"),
            description = "Полная 15-минутная версия открытого анимационного фильма Blender Foundation. Это не трейлер.",
            licenseInfo = "Blender Foundation • CC BY 3.0 • https://durian.blender.org/about/",
            posterColorStart = 0xFF3B1D38,
            posterColorEnd = 0xFF140D1A,
            posterAccentColor = 0xFFFF754C,
            directVariants = listOf(
                StreamVariant(
                    id = "open_sintel_mkv",
                    sourceName = "Blender Foundation",
                    audioTrack = "Оригинал (ENG)",
                    quality = "1080p • MKV",
                    streamUrl = "https://download.blender.org/demo/movies/Sintel.2010.1080p.mkv"
                )
            ),
            isDemoContent = false
        ),
        MediaItem(
            id = "open_movies_bunny_full",
            title = "Большой кролик Бак — полный фильм",
            originalTitle = "Big Buck Bunny",
            year = 2008,
            type = MediaType.MOVIE,
            rating = "—",
            genres = listOf("Анимация", "Комедия", "Семейный"),
            description = "Полный открытый анимационный фильм Blender Foundation, около 10 минут. Поток Wikimedia Commons.",
            licenseInfo = "Blender Foundation • CC BY 3.0 • https://peach.blender.org/about/",
            posterColorStart = 0xFF283044,
            posterColorEnd = 0xFF111520,
            posterAccentColor = 0xFFA78BFA,
            directVariants = listOf(
                StreamVariant(
                    id = "open_bbb_webm",
                    sourceName = "Wikimedia Commons",
                    audioTrack = "Оригинал",
                    quality = "720p • WebM/VP9",
                    streamUrl = "https://upload.wikimedia.org/wikipedia/commons/transcoded/c/c0/Big_Buck_Bunny_4K.webm/Big_Buck_Bunny_4K.webm.720p.vp9.webm"
                )
            ),
            isDemoContent = false
        )
    )

    override suspend fun getRecentFeed(): List<MediaItem> = films

    override suspend fun search(query: String): List<MediaItem> {
        val needle = query.trim().lowercase()
        return films.filter { needle.isEmpty() || it.title.lowercase().contains(needle) ||
            it.originalTitle.lowercase().contains(needle) ||
            it.genres.any { genre -> genre.lowercase().contains(needle) } }
    }

    override suspend fun getDetails(mediaId: String): MediaItem? = films.find { it.id == mediaId }
}
