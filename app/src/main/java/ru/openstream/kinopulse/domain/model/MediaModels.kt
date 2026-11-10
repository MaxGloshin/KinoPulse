package ru.openstream.kinopulse.domain.model

enum class MediaType(val labelRu: String) {
    MOVIE("Фильм"),
    SERIES("Сериал")
}

data class StreamVariant(
    val id: String,
    val sourceName: String,
    val audioTrack: String,
    val quality: String,
    val streamUrl: String,
    val isOfflineDemo: Boolean = false
)

data class EpisodeItem(
    val number: Int,
    val title: String,
    val durationMinutes: Int,
    val variants: List<StreamVariant>
)

data class SeasonItem(
    val number: Int,
    val title: String,
    val episodes: List<EpisodeItem>
)

data class MediaItem(
    val id: String,
    val title: String,
    val originalTitle: String,
    val year: Int,
    val type: MediaType,
    val rating: String,
    val genres: List<String>,
    val description: String,
    val licenseInfo: String,
    val posterColorStart: Long,
    val posterColorEnd: Long,
    val posterAccentColor: Long,
    val directVariants: List<StreamVariant> = emptyList(),
    val seasons: List<SeasonItem> = emptyList(),
    val isDemoContent: Boolean = true
)
