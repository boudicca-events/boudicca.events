package events.boudicca.enricher.service.musicbrainz

import base.boudicca.SemanticKeys
import base.boudicca.enricher.service.Enricher
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream

@Service
class MusicBrainzArtistEnricher
@Autowired
constructor(
    @Value("\${boudicca.enricher.musicbrainz.data.path:}") musicBrainzDataPath: String?,
    @Value("\${boudicca.enricher.musicbrainz.index.path:}") musicBrainzIndexPath: String?,
) : Enricher {
    private val logger = KotlinLogging.logger {}

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    private val artistMatcher = createArtistMatcher(musicBrainzDataPath, musicBrainzIndexPath)

    override fun enrich(event: StructuredEvent): StructuredEvent {
        if (artistMatcher == null) {
            return event
        }
        return doEnrich(event, artistMatcher)
    }

    private fun doEnrich(event: StructuredEvent, artistMatcher: ArtistMatcher): StructuredEvent {
        if (event.getProperty(SemanticKeys.CATEGORY_PROPERTY).firstOrNull()?.second != EventCategory.MUSIC) {
            return event
        }
        val foundArtists = artistMatcher.findArtists(event.name)
        if (foundArtists.isNotEmpty()) {
            val nonSubstringArtists =
                foundArtists.filter { artist ->
                    foundArtists.none { it.name.length != artist.name.length && it.name.contains(artist.name, true) }
                }
            return insertArtistData(event, nonSubstringArtists)
        }
        return event
    }

    private fun insertArtistData(e: StructuredEvent, artists: List<Artist>): StructuredEvent {
        val builder = e.toBuilder()

        if (e.getProperty(SemanticKeys.CONCERT_BANDLIST_PROPERTY).isEmpty()) {
            builder.withProperty(SemanticKeys.CONCERT_BANDLIST_PROPERTY, artists.map { it.name })
        }

        val genre = artists.firstNotNullOfOrNull { it.genre }
        if (e.getProperty(SemanticKeys.CONCERT_GENRE_PROPERTY).isEmpty()) {
            builder.withProperty(SemanticKeys.CONCERT_GENRE_PROPERTY, genre)
        }

        return builder.build()
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
            logger.debug { "no musicBrainzDataPath given, disabling enricher" }
            return null
        }
        val file = File(musicBrainzDataPath)
        if (!file.exists() || !file.isFile || !file.canRead()) {
            error("musicbrainz data path $musicBrainzDataPath is not a readable file!")
        }
        return objectMapper
            .readValue(
                BufferedInputStream(GZIPInputStream(FileInputStream(file))),
                object : TypeReference<List<Artist>?>() {},
            )
    }

    private fun loadIndex(musicBrainzIndexPath: String?): ByteBuffer? {
        if (musicBrainzIndexPath.isNullOrBlank()) {
            logger.debug { "no musicBrainzIndexPath given, disabling enricher" }
            return null
        }
        val file = File(musicBrainzIndexPath)
        if (!file.exists() || !file.isFile || !file.canRead()) {
            error("musicbrainz index path $musicBrainzIndexPath is not a readable file!")
        }
        return ByteBuffer.wrap(BufferedInputStream(GZIPInputStream(FileInputStream(file))).readBytes())
    }
}
