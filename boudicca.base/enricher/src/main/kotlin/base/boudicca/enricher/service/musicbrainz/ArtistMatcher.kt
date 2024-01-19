package base.boudicca.enricher.service.musicbrainz

import java.nio.ByteBuffer

class ArtistMatcher(private val artists: List<Artist>, private val index: ByteBuffer) {
    fun findArtists(string: String): List<Artist> {
        if (string.isEmpty()) {
            return emptyList()
        }

        val lowerString = string.lowercase()

        //TODO duplicated artists found?
        val foundArtists = mutableListOf<Artist>()
        foundArtists.addAll(matchArtistsFrom(lowerString, 0))
        for (i in lowerString.indices) {
            if (!lowerString[i].isLetterOrDigit()) {
                if (i + 1 < lowerString.length) {
                    foundArtists.addAll(matchArtistsFrom(lowerString, i + 1))
                }
            }
        }

        return foundArtists
    }

    private fun matchArtistsFrom(string: String, stringIndex: Int): List<Artist> {
        val matchedArtists = mutableListOf<Artist>()

        var min = 0
        var max = artists.size - 1
        while (min <= max) {
            val next = (min + max) / 2
            val compare = compare(string, stringIndex, next)
            if (compare == 0) {
                //TODO what about multiple matches?
                matchedArtists.add(artists[index.getInt(next * 4)])
                break
            } else if (compare < 0) {
                max = next - 1
            } else {
                min = next + 1
            }
        }

        return matchedArtists
    }

    private fun compare(string: String, stringIndex: Int, indexIndex: Int): Int {
        val artistName = artists[index.getInt(indexIndex * 4)].lowercaseName

        for (i in artistName.indices) {
            val currentStringIndex = stringIndex + i
            if (currentStringIndex >= string.length) {
                return -1
            }
            if (string[currentStringIndex] < artistName[i]) {
                return -1
            }
            if (string[currentStringIndex] > artistName[i]) {
                return 1
            }
        }

        //substrings matches! now look if we are at a word boundary
        if (stringIndex + artistName.length == string.length) {
            return 0
        }
        if (string[stringIndex + artistName.length].isLetterOrDigit()) {
            return 1
        }
        return 0
    }
}