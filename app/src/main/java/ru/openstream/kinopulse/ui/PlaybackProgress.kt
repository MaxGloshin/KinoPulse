package ru.openstream.kinopulse.ui

data class WatchProgressEntry(
    val mediaId: String,
    val variantId: String,
    val title: String,
    val subtitle: String,
    val positionMs: Long
)

/**
 * Saving a position updates one film/episode. Passing null means playback finished,
 * so it must disappear from "Continue watching" without removing other episodes.
 */
internal fun updateWatchProgress(
    progress: Map<String, WatchProgressEntry>,
    key: String,
    entry: WatchProgressEntry?
): Map<String, WatchProgressEntry> {
    if (key.isBlank()) return progress
    return progress.toMutableMap().apply {
        if (entry == null) remove(key) else put(key, entry)
    }
}
