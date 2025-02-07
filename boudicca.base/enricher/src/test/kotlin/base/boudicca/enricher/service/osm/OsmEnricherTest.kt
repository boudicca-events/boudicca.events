package base.boudicca.enricher.service.osm

import base.boudicca.SemanticKeys
import base.boudicca.fetcher.Fetcher
import base.boudicca.fetcher.HttpClientWrapper
import base.boudicca.model.structured.StructuredEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class OsmEnricherTest {

    private val enricher = OsmEnricher(fetcher = Fetcher(httpClient = object: HttpClientWrapper {
        override fun doGet(url: String): Pair<Int, String> {
            if (url.contains("1860773")){
                return Pair(200,"""{"elements": [{"center": {"lat": 48.3117234, "lon": 14.3117584}, "id": 1860773, "members": [{"ref": 132410663, "role": "outer", "type": "way"}, {"ref": 132410664, "role": "inner", "type": "way"}], "tags": {"addr:city": "Linz", "addr:country": "AT", "addr:housenumber": "43", "addr:postcode": "4020", "addr:street": "Posthofstra\u00dfe", "amenity": "arts_centre", "building": "yes", "contact:email": "office@posthof.at", "contact:fax": "+43 732 782652", "contact:phone": "+43 732 7705480", "contact:website": "https://www.posthof.at", "logo": "https://www.inskabarett.at/fileadmin/user_upload/inskabarett_artist_events/eventPlace/images/55-9d773a529adc.jpg", "name": "Posthof", "type": "multipolygon", "wikidata": "Q1434089", "wikipedia": "de:Posthof (Linz)"}, "type": "relation"}], "generator": "Overpass API 0.7.62.4 2390de5a", "osm3s": {"copyright": "The data included in this document is from www.openstreetmap.org. The data is made available under ODbL.", "timestamp_osm_base": "2025-02-07T12:06:05Z"}, "version": 0.6}""")
            }
            return Pair(400,"")
        }

        override fun doPost(url: String, contentType: String, content: String): Pair<Int, String> {
            throw NotImplementedError("this enricher does not use post")
        }

    }))

    @Test
    fun testNoop() {
        val event = StructuredEvent.builder("Test Event", OffsetDateTime.now()).build()

        assertEquals(event, enricher.enrich(event))
    }

    @Test
    fun `test structured event with osm_id provided`() {
        val event = StructuredEvent
            .builder("Test Event", OffsetDateTime.now())
            .withProperty(SemanticKeys.LOCATION_OSM_ID_PROPERTY, "1860773")
            .build()
        val enrichedEvent = enricher.enrich(event)

        assertTrue(enrichedEvent.getProperty(SemanticKeys.LOCATION_NAME_PROPERTY).isNotEmpty())
    }

}
