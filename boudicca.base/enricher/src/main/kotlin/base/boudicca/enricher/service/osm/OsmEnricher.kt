package base.boudicca.enricher.service.osm

import base.boudicca.Property
import base.boudicca.SemanticKeys
import base.boudicca.enricher.service.Enricher
import base.boudicca.enricher.service.EnricherOrderConstants
import base.boudicca.fetcher.Fetcher
import base.boudicca.fetcher.FetcherEventListener
import base.boudicca.fetcher.InMemoryFetcherCache
import base.boudicca.format.ListFormat
import base.boudicca.format.UrlUtils
import base.boudicca.format.UrlUtils.encodeURL
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.StructuredEvent.StructuredEventBuilder
import base.boudicca.model.structured.Variant
import base.boudicca.model.structured.VariantConstants.SOURCE_VARIANT_NAME
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit


@Service
@Order(EnricherOrderConstants.OsmEnricherOrder)
class OsmEnricher(
    private val fetcher: Fetcher = createDefaultFetcher()
) : Enricher {
    val sourceVariant = listOf(Variant(SOURCE_VARIANT_NAME, "OSM"))
    val logger = LoggerFactory.getLogger(this::class.java)!!

    val mapper = jacksonObjectMapper()

    override fun enrich(event: StructuredEvent): StructuredEvent {
        val osmId: String? = getOrSearchOsmId(event)
        if (osmId == null || !isValidOsmIdFormat(osmId)) {
            return event
        }
        val enrichedEvent = enrichEventFromOverpass(osmId, event)

        return enrichedEvent
    }

    @Suppress("detekt.LongMethod")
    private fun enrichEventFromOverpass(
        osmId: String,
        event: StructuredEvent
    ): StructuredEvent {
        val query = buildOverPassQuery(osmId)
        val url = "https://overpass-api.de/api/interpreter?data=${query.encodeURL()}"
        val response = synchronized(fetcher) {
            fetcher.fetchUrl(url)
        }

        // parse osm data into our properties
        val builder = event.toBuilder()
        val json = mapper.readTree(response)
        val elements = json["elements"] as ArrayNode
        if (elements.isEmpty) {
            logger.warn("overpass did not return any results for osm id $osmId")
            return event
        }
        val firstElement = elements.first()
        val tags = firstElement["tags"]
        val center = firstElement["center"]


        val currentSources = ListFormat.parseFromString(event.data[SemanticKeys.SOURCES_PROPERTY.getKey()])
        builder.withProperty(
            property = SemanticKeys.SOURCES_PROPERTY,
            value = currentSources + "Source:OSM:${json["osm3s"]["copyright"].asText()}"
        )

        builder.updatePropertyIfEmpty(
            event,
            SemanticKeys.LOCATION_COORDINATES_LAT_PROPERTY,
            center?.get("lat")?.asDouble(),
        )
        builder.updatePropertyIfEmpty(
            event,
            SemanticKeys.LOCATION_COORDINATES_LON_PROPERTY,
            center?.get("lon")?.asDouble(),
        )
        builder.updatePropertyIfEmpty(
            event,
            SemanticKeys.LOCATION_OSM_ID_PROPERTY,
            osmId
        )
        if (tags != null) {
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_NAME_PROPERTY,
                tags["name"]?.asText()
            )
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_ADDRESS_PROPERTY,
                buildAddress(tags)
            )
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_CITY_PROPERTY,
                tags["addr:city"]?.asText()
            )
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_DESCRIPTION_PROPERTY,
                tags["amenity"]?.asText()
            )
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_CONTACT_EMAIL_PROPERTY,
                tags["contact:email"]?.asText()
            )
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_CONTACT_PHONE_PROPERTY,
                tags["contact:phone"]?.asText()
            )
            try {
                builder.updatePropertyIfEmpty(
                    event,
                    SemanticKeys.LOCATION_URL_PROPERTY,
                    UrlUtils.parse(tags["contacts:website"]?.asText())
                )
            } catch (_: IllegalArgumentException) {
                logger.info("Could not map ${tags["contacts:website"].asText()} to URI")
            }
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_WIKIPEDIA_PROPERTY,
                tags["wikipedia"]?.asText()
            )
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_WIKIDATA_PROPERTY,
                tags["wikidata"]?.asText()
            )
        }

        return builder.build()
    }

    private fun buildOverPassQuery(osmId: String): String {
        val idType = when (osmId.toCharArray()[0].lowercaseChar()) {
            'n' -> "node"
            'w' -> "way"
            'r' -> "relation"
            else -> throw IllegalStateException("osmId was validated before, should not be able to have invalid char in beginning: $osmId")
        }
        return "[out:json];$idType(${osmId.substring(1)});out body center;"
    }

    private fun getOrSearchOsmId(event: StructuredEvent): String? {
        // TODO maybe think about taking all osmIds and not only the first :)
        val osmIds = event.getProperty(SemanticKeys.LOCATION_OSM_ID_PROPERTY)
        val osmId = if (osmIds.isNotEmpty()) {
            osmIds.first().second
        } else {
            //TODO search for an osmId
            null
        }
        return osmId
    }

    private fun buildAddress(tags: JsonNode): String {
        val street = ((tags["addr:street"]?.asText() ?: "") + " " + (tags["addr:housenumber"]?.asText() ?: "")).trim()
        val postcode = tags["addr:postcode"]?.asText()
        val city = tags["addr:city"]?.asText()
        return listOfNotNull(street.ifBlank { null }, postcode, city).joinToString(", ")
    }

    private fun <P> StructuredEventBuilder.updatePropertyIfEmpty(
        event: StructuredEvent,
        property: Property<P>,
        newValue: P?
    ): StructuredEventBuilder {
        val newValueIsNotNullOrBlank = newValue != null && (newValue !is String || newValue.isNotBlank())
        if (event.data[property.getKey()].isNullOrBlank() && newValueIsNotNullOrBlank) {
            this.withProperty(
                property = property,
                value = newValue,
                variants = sourceVariant
            )
        }
        return this
    }

    private fun isValidOsmIdFormat(osmId: String): Boolean = osmId.matches(Regex("^[NRWnrw]?\\d+$"))
}

fun createDefaultFetcher(): Fetcher {
    return Fetcher(
        manualSetDelay = TimeUnit.SECONDS.toMillis(1),
        fetcherCache = InMemoryFetcherCache(), //TODO do we want to persist this cache maybe?
    )
}
