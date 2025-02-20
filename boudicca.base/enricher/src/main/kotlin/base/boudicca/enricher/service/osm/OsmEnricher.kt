package base.boudicca.enricher.service.osm

import base.boudicca.Property
import base.boudicca.SemanticKeys
import base.boudicca.enricher.service.Enricher
import base.boudicca.enricher.service.EnricherOrderConstants
import base.boudicca.fetcher.Fetcher
import base.boudicca.fetcher.InMemoryFetcherCache
import base.boudicca.format.ListFormat
import base.boudicca.format.UrlUtils
import base.boudicca.format.UrlUtils.encodeURL
import base.boudicca.model.structured.Key
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


private const val NOMINATIM_BASE_URL = "https://nominatim.boudicca.events"

@Service
@Order(EnricherOrderConstants.OsmEnricherOrder)
class OsmEnricher(
    private val fetcher: Fetcher = createDefaultFetcher()
) : Enricher {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
        val mapper = jacksonObjectMapper()
    }

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
        val url = "$NOMINATIM_BASE_URL/lookup?format=jsonv2&extratags=1&namedetails=1&osm_ids=${osmId}"
        val response = synchronized(fetcher) {
            fetcher.fetchUrl(url)
        }

        // parse osm data into our properties
        val builder = event.toBuilder()
        val json = mapper.readTree(response)
        if (json.isEmpty) {
            logger.warn("nominatim did not return any results for osm id $osmId")
            return event
        }
        val nominatimPlace = json.single()
        val tags = nominatimPlace["extratags"]
        val address = nominatimPlace["address"]

        val currentSources = ListFormat.parseFromString(event.data[SemanticKeys.SOURCES_PROPERTY.getKey()])
        builder.withProperty(
            property = SemanticKeys.SOURCES_PROPERTY,
            value = currentSources + "Source:OSM:${nominatimPlace["licence"]?.asText()}"
        )

        builder.updatePropertyIfEmpty(
            event,
            SemanticKeys.LOCATION_COORDINATES_LAT_PROPERTY,
            nominatimPlace["lat"]?.asDouble(),
        )
        builder.updatePropertyIfEmpty(
            event,
            SemanticKeys.LOCATION_COORDINATES_LON_PROPERTY,
            nominatimPlace["lon"].asDouble(),
        )
        builder.updatePropertyIfEmpty(
            event,
            SemanticKeys.LOCATION_NAME_PROPERTY,
            nominatimPlace["name"]?.asText()
        )
        builder.updatePropertyIfEmpty(
            event,
            SemanticKeys.LOCATION_DESCRIPTION_PROPERTY,
            nominatimPlace["category"]?.asText()
        )
        builder.updatePropertyIfEmpty(
            event,
            SemanticKeys.LOCATION_OSM_ID_PROPERTY,
            osmId
        )
        if (address != null) {
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_ADDRESS_PROPERTY,
                buildAddress(address)
            )
            builder.updatePropertyIfEmpty(
                event,
                SemanticKeys.LOCATION_CITY_PROPERTY,
                address["city"]?.asText()
            )
        }
        if (tags != null) {
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
            val websiteText = (tags["contact:website"] ?: tags["website"])?.asText()
            try {
                builder.updatePropertyIfEmpty(
                    event,
                    SemanticKeys.LOCATION_URL_PROPERTY,
                    UrlUtils.parse(websiteText)
                )
            } catch (_: IllegalArgumentException) {
                logger.info("Could not map $websiteText to URI")
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

    private fun getOrSearchOsmId(event: StructuredEvent): String? {
        // TODO maybe think about taking all osmIds and not only the first :)
        val osmIds = event.getProperty(SemanticKeys.LOCATION_OSM_ID_PROPERTY)
        return if (osmIds.isNotEmpty()) {
            osmIds.first().second
        } else {
            searchOsmId(event)
        }
    }

    private fun searchOsmId(event: StructuredEvent): String? {
        val locationNames = event
            .getProperty(SemanticKeys.LOCATION_NAME_PROPERTY)
            .map(Pair<Key, String>::second)
        if (locationNames.isEmpty()) {
            return null
        }
        val locationCities = event
            .getProperty(SemanticKeys.LOCATION_CITY_PROPERTY)
            .map(Pair<Key, String>::second)

        val additionalQuery = locationCities.ifEmpty {
            event
                .getProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY)
                .map(Pair<Key, String>::second)
        }

        val locationQuery = (locationNames + additionalQuery)
            .filter { it.isNotBlank() }
            .joinToString(separator = ",")

        val query =
            """$NOMINATIM_BASE_URL/search?accept-language=de,en&format=jsonv2&limit=1&q=${locationQuery.encodeURL()}"""
                .trim()
        val response = fetcher.fetchUrl(query)

        val json = mapper.readTree(response) as ArrayNode

        return if (json.isEmpty) {
            logNotFound(locationQuery)
            null
        } else {
            val nominatimPlace = json.first()
            val type = nominatimPlace["osm_type"]?.asText()
            val id = nominatimPlace["osm_id"]?.asText()
            if (!type.isNullOrBlank() && !id.isNullOrBlank()) {
                type[0] + id
            } else {
                logNotFound(query)
                null
            }
        }
    }

    private fun logNotFound(locationQuery: String) {
        logger.info("did not find any location for query: $locationQuery")
    }

    private fun buildAddress(tags: JsonNode): String {
        val street = ((tags["road"]?.asText() ?: "") + " " + (tags["house_number"]?.asText() ?: "")).trim()
        val postcode = tags["postcode"]?.asText()
        val city = tags["city"]?.asText()
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
                variants = listOf(Variant(SOURCE_VARIANT_NAME, "OSM"))
            )
        }
        return this
    }

    private fun isValidOsmIdFormat(osmId: String): Boolean = osmId.matches(Regex("^[NRWnrw]?\\d+$"))
}

fun createDefaultFetcher(): Fetcher {
    return Fetcher(
        manualSetDelay = 0,
        fetcherCache = InMemoryFetcherCache()
    )
}
