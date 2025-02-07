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
            if (url.contains("R1860773", true)){
                @Suppress("detekt.MaxLineLength")
                return Pair(200,"""[{"place_id":3023472,"licence":"Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright","osm_type":"relation","osm_id":1860773,"lat":"48.311720550000004","lon":"14.311656722324702","category":"amenity","type":"arts_centre","place_rank":30,"importance":0.21549815043721327,"addresstype":"amenity","name":"Posthof","display_name":"Posthof, 43, Posthofstraße, Kaplanhofviertel, Kaplanhof, Linz, Upper Austria, 4020, Austria","address":{"amenity":"Posthof","house_number":"43","road":"Posthofstraße","neighbourhood":"Kaplanhofviertel","suburb":"Kaplanhof","city":"Linz","state":"Upper Austria","ISO3166-2-lvl4":"AT-4","postcode":"4020","country":"Austria","country_code":"at"},"extratags":{"logo": "https://www.inskabarett.at/fileadmin/user_upload/inskabarett_artist_events/eventPlace/images/55-9d773a529adc.jpg", "building": "yes", "wikidata": "Q1434089", "wikipedia": "de:Posthof (Linz)", "contact:fax": "+43 732 782652", "contact:email": "office@posthof.at", "contact:phone": "+43 732 7705480", "contact:website": "https://www.posthof.at"},"namedetails":{"name": "Posthof"},"boundingbox":["48.3113042","48.3121426","14.3112429","14.3122740"]}]""")
            }
            if (url.contains("Posthof", true)){
                @Suppress("detekt.MaxLineLength")
                return Pair(200,"""[{"place_id":3023472,"licence":"Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright","osm_type":"relation","osm_id":1860773,"lat":"48.311720550000004","lon":"14.311656722324702","category":"amenity","type":"arts_centre","place_rank":30,"importance":0.21549815043721327,"addresstype":"amenity","name":"Posthof","display_name":"Posthof, 43, Posthofstraße, Kaplanhofviertel, Kaplanhof, Linz, Oberösterreich, 4020, Österreich","boundingbox":["48.3113042","48.3121426","14.3112429","14.3122740"]}]""")
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
            .withProperty(SemanticKeys.LOCATION_OSM_ID_PROPERTY, "R1860773")
            .build()
        val enrichedEvent = enricher.enrich(event)

        assertTrue(enrichedEvent.getProperty(SemanticKeys.LOCATION_NAME_PROPERTY).isNotEmpty())
        assertTrue(enrichedEvent.getProperty(SemanticKeys.LOCATION_CITY_PROPERTY).isNotEmpty())
    }

    @Test
    fun `test structured event without osmId will lookup`() {
        val event = StructuredEvent
            .builder("Test Event", OffsetDateTime.now())
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Posthof")
            .build()
        val enrichedEvent = enricher.enrich(event)

        assertTrue(enrichedEvent.getProperty(SemanticKeys.LOCATION_OSM_ID_PROPERTY).isNotEmpty())
        assertTrue(enrichedEvent.getProperty(SemanticKeys.LOCATION_CITY_PROPERTY).isNotEmpty())
    }

}
