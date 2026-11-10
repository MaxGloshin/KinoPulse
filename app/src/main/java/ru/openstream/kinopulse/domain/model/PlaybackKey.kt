package ru.openstream.kinopulse.domain.model

fun MediaItem.playbackKey(variant: StreamVariant): String {
    for (season in seasons) {
        for (episode in season.episodes) {
            if (episode.variants.any { it.id == variant.id }) {
                return "$id/${season.number}/${episode.number}"
            }
        }
    }
    return id
}
