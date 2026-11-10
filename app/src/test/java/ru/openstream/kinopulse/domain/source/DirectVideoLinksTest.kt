package ru.openstream.kinopulse.domain.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectVideoLinksTest {
    @Test fun acceptsHttpsWithQueryString() {
        assertEquals(
            "https://example.org/video/master.m3u8?expires=123",
            DirectVideoLinks.normalize(" https://example.org/video/master.m3u8?expires=123 ")
        )
    }

    @Test fun rejectsHttpOrInvalidLinks() {
        assertNull(DirectVideoLinks.normalize("http://example.org/movie.mp4"))
        assertNull(DirectVideoLinks.normalize("file:///storage/emulated/0/movie.mp4"))
        assertNull(DirectVideoLinks.normalize("javascript:alert(1)"))
        assertNull(DirectVideoLinks.normalize("https://user:secret@example.org/movie.mp4"))
        assertNull(DirectVideoLinks.normalize("not a link"))
    }

    @Test fun stableIdsAreDeterministicAndDistinct() {
        val first = DirectVideoLinks.stableId("https://example.org/a.mp4")
        assertEquals(first, DirectVideoLinks.stableId("https://example.org/a.mp4"))
        assertTrue(first.startsWith("user_link_"))
        assertNotEquals(first, DirectVideoLinks.stableId("https://example.org/b.mp4"))
    }
}
