package base.boudicca.enricher.service.musicbrainz

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class ArtistMatcherTest {

    @Test
    fun emptyInput() {
        val artistMatcher = testMatcher(emptyList(), ByteArray(0))

        val result = artistMatcher.findArtists("test")

        assertEquals(0, result.size)
    }

    @Test
    fun emptyString() {
        val artistMatcher = testMatcher(emptyList(), ByteArray(0))

        val result = artistMatcher.findArtists("")

        assertEquals(0, result.size)
    }

    @Test
    fun testSingleArtistMatch() {
        val artistMatcher = testMatcher(
            listOf(
                Artist("artist", null)
            ),
            byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
            )
        )

        val result = artistMatcher.findArtists("artist")

        assertEquals(1, result.size)
        assertEquals("artist", result[0].name)
    }

    @Test
    fun testThreeArtists() {
        val artistMatcher = testMatcher(
            listOf(
                Artist("b", null),
                Artist("a", null),
                Artist("c", null),
            ),
            byteArrayOf(
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x02,
            )
        )

        var result = artistMatcher.findArtists("a")
        assertEquals(1, result.size)
        assertEquals("a", result[0].name)

        result = artistMatcher.findArtists("b")
        assertEquals(1, result.size)
        assertEquals("b", result[0].name)

        result = artistMatcher.findArtists("c")
        assertEquals(1, result.size)
        assertEquals("c", result[0].name)
    }

    @Test
    fun testThreeArtistsMatch() {
        val artistMatcher = testMatcher(
            listOf(
                Artist("a", null),
                Artist("b", null),
                Artist("c", null),
            ),
            byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x02,
            )
        )

        val result = artistMatcher.findArtists("a b c")
        assertEquals(3, result.size)
        assertTrue(result.any { it.name == "a" })
        assertTrue(result.any { it.name == "b" })
        assertTrue(result.any { it.name == "c" })
    }

    @Test
    fun testLongerArtistMatch() {
        val artistMatcher = testMatcher(
            listOf(
                Artist("artist fancy name", null),
                Artist("b", null),
                Artist("c", null),
            ),
            byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x02,
            )
        )

        val result = artistMatcher.findArtists("this is artist fancy name show")
        assertEquals(1, result.size)
        assertEquals("artist fancy name", result[0].name)
    }

    @Test
    fun testCaseInsensitive() {
        val artistMatcher = testMatcher(
            listOf(
                Artist("a", null),
                Artist("b", null),
                Artist("c", null),
            ),
            byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x02,
            )
        )

        val result = artistMatcher.findArtists("A")
        assertEquals(1, result.size)
        assertEquals("a", result[0].name)
    }

    @Test
    fun testNoSubmatch() {
        val artistMatcher = testMatcher(
            listOf(
                Artist("a", null),
            ),
            byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
            )
        )

        val result = artistMatcher.findArtists("ab")
        assertEquals(0, result.size)
    }

    @Test
    fun testSubmatchWithSpecialChar() {
        val artistMatcher = testMatcher(
            listOf(
                Artist("a", null),
            ),
            byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
            )
        )

        val result = artistMatcher.findArtists("a&b")
        assertEquals(1, result.size)
    }

    @Test
    fun testArtistWithSpecialChar() {
        val artistMatcher = testMatcher(
            listOf(
                Artist("a&b", null),
                Artist("a - b", null),
            ),
            byteArrayOf(
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
            )
        )

        var result = artistMatcher.findArtists("show of a&b wow")
        assertEquals(1, result.size)
        assertEquals("a&b", result[0].name)
        result = artistMatcher.findArtists("show of a - b wow")
        assertEquals(1, result.size)
        assertEquals("a - b", result[0].name)
    }

    private fun testMatcher(artists: List<Artist>, bytes: ByteArray): ArtistMatcher {
        return ArtistMatcher(artists, ByteBuffer.wrap(bytes))
    }
}
