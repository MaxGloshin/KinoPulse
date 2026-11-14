package ru.openstream.kinopulse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackProgressTest {
    private fun progress(mediaId: String, seconds: Long) =
        WatchProgressEntry(mediaId, "source", mediaId, "720p", seconds * 1000)

    @Test fun updatingPositionPreservesOtherEpisodes() {
        val first = progress("series", 20)
        val second = progress("series", 35)
        val initial = mapOf("series/1/1" to first, "series/1/2" to second)
        val updated = updateWatchProgress(initial, "series/1/1", progress("series", 42))

        assertEquals(42_000L, updated["series/1/1"]?.positionMs)
        assertEquals(second, updated["series/1/2"])
        assertEquals(first, initial["series/1/1"])
    }

    @Test fun finishedEpisodeLeavesOtherEntriesUntouched() {
        val initial = mapOf(
            "series/1/1" to progress("series", 42),
            "series/1/2" to progress("series", 15),
            "movie" to progress("movie", 28)
        )
        val completed = updateWatchProgress(initial, "series/1/1", null)

        assertFalse(completed.containsKey("series/1/1"))
        assertTrue(completed.containsKey("series/1/2"))
        assertTrue(completed.containsKey("movie"))
        assertEquals(3, initial.size)
    }

    @Test fun replayAfterCompletionCanSaveFromBeginning() {
        val completed = updateWatchProgress(mapOf("movie" to progress("movie", 90)), "movie", null)
        val restarted = updateWatchProgress(completed, "movie", progress("movie", 3))

        assertFalse(completed.containsKey("movie"))
        assertEquals(3_000L, restarted["movie"]?.positionMs)
    }

    @Test fun emptyKeyDoesNotOverwriteProgress() {
        val original = mapOf("movie" to progress("movie", 12))
        assertEquals(original, updateWatchProgress(original, "", null))
    }
}
