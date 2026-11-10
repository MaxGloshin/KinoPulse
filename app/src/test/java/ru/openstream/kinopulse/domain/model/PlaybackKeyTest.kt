package ru.openstream.kinopulse.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PlaybackKeyTest {
    private fun variant(id: String) = StreamVariant(id, "demo", "original", "MP4", "https://example.org/$id.mp4")
    private val first = variant("first")
    private val alternate = variant("alternate")
    private val second = variant("second")
    private val nextSeason = variant("next-season")
    private val series = MediaItem(
        id = "series", title = "Demo", originalTitle = "Demo", year = 2026,
        type = MediaType.SERIES, rating = "", genres = emptyList(), description = "",
        licenseInfo = "", posterColorStart = 0, posterColorEnd = 0, posterAccentColor = 0,
        seasons = listOf(
            SeasonItem(1, "First", listOf(
                EpisodeItem(1, "One", 1, listOf(first, alternate)),
                EpisodeItem(2, "Two", 1, listOf(second))
            )),
            SeasonItem(2, "Second", listOf(EpisodeItem(1, "One", 1, listOf(nextSeason))))
        )
    )

    @Test fun qualitiesOfSameEpisodeSharePosition() {
        assertEquals(series.playbackKey(first), series.playbackKey(alternate))
    }

    @Test fun episodesAndSeasonsHaveSeparatePositions() {
        assertNotEquals(series.playbackKey(first), series.playbackKey(second))
        assertNotEquals(series.playbackKey(first), series.playbackKey(nextSeason))
    }

    @Test fun movieVariantsSharePosition() {
        val movie = series.copy(id = "movie", type = MediaType.MOVIE, seasons = emptyList(), directVariants = listOf(first, alternate))
        assertEquals("movie", movie.playbackKey(first))
        assertEquals(movie.playbackKey(first), movie.playbackKey(alternate))
    }
}
