package base.boudicca.enricher.service.musicbrainz

import base.boudicca.SemanticKeys
import base.boudicca.enricher.service.Enricher
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream

@Service
class MusicBrainzArtistEnricher @Autowired constructor(
    @Value("\${boudicca.enricher.musicbrainz.data.path:}") musicBrainzDataPath: String?,
    @Value("\${boudicca.enricher.musicbrainz.index.path:}") musicBrainzIndexPath: String?,
) : Enricher {

    private val LOG = LoggerFactory.getLogger(this.javaClass)

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    private val artistMatcher = createArtistMatcher(musicBrainzDataPath, musicBrainzIndexPath)

    override fun enrich(e: Event): Event {
        if (artistMatcher == null) {
            return e
        }
        return doEnrich(e, artistMatcher)
    }

    private fun doEnrich(e: Event, artistMatcher: ArtistMatcher): Event {
        if (e.data[SemanticKeys.CATEGORY] != EventCategory.MUSIC.name) {
            return e
        }
        val foundArtists = artistMatcher.findArtists(e.name)
        if (foundArtists.isNotEmpty()) {
            val nonSubstringArtists = foundArtists.filter { artist ->
                foundArtists.none { it.name.length != artist.name.length && it.name.contains(artist.name, true) }
            }
            return insertArtistData(e, nonSubstringArtists)
        }
        return e
    }

    private fun insertArtistData(e: Event, artists: List<Artist>): Event {
        val enrichedData = e.data.toMutableMap()
        enrichedData[SemanticKeys.CONCERT_BANDLIST] = artists.joinToString("\n") { it.name }
        val genre = artists.firstNotNullOfOrNull { it.genre }
        if (genre != null && !enrichedData.containsKey(SemanticKeys.CONCERT_GENRE)) {
            enrichedData[SemanticKeys.CONCERT_GENRE] = genre
        }
        return Event(e.name, e.startDate, enrichedData)
    }

    private fun createArtistMatcher(musicBrainzDataPath: String?, musicBrainzIndexPath: String?): ArtistMatcher? {
        val data = loadData(musicBrainzDataPath)
        val index = loadIndex(musicBrainzIndexPath)
        if (data != null && index != null) {
            return ArtistMatcher(data, index)
        }
        return null
    }

    private fun loadData(musicBrainzDataPath: String?): List<Artist>? {
        if (musicBrainzDataPath.isNullOrBlank()) {
            LOG.debug("no musicBrainzDataPath given, disabling enricher")
            return null
        }
        val file = File(musicBrainzDataPath)
        if (!file.exists() || !file.isFile || !file.canRead()) {
            throw IllegalArgumentException("musicbrainz data path $musicBrainzDataPath is not a readable file!")
        }
        return objectMapper
            .readValue(BufferedInputStream(GZIPInputStream(FileInputStream(file))), object : TypeReference<List<Artist>?>() {})
    }

    private fun loadIndex(musicBrainzIndexPath: String?): ByteBuffer? {
        if (musicBrainzIndexPath.isNullOrBlank()) {
            LOG.debug("no musicBrainzIndexPath given, disabling enricher")
            return null
        }
        val file = File(musicBrainzIndexPath)
        if (!file.exists() || !file.isFile || !file.canRead()) {
            throw IllegalArgumentException("musicbrainz index path $musicBrainzIndexPath is not a readable file!")
        }
        return ByteBuffer.wrap(BufferedInputStream(GZIPInputStream(FileInputStream(file))).readBytes())
    }

}
