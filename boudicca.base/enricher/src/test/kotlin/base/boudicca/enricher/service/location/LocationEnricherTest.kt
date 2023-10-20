package base.boudicca.enricher.service.location

import base.boudicca.SemanticKeys
import base.boudicca.enricher.model.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class LocationEnricherTest {

    @Test
    fun testNoop() {
        val event = createTestEvent()
        val locationEnricher = createNoopEnricher()
        val enrichedEvent = locationEnricher.enrich(event)

        assertEquals(event, enrichedEvent)
    }

    @Test
    fun testSimpleLocationMatch() {
        val event = createTestEvent()
        val locationEnricher = createTestEnricher(
            listOf(
                mapOf(
                    SemanticKeys.LOCATION_NAME to listOf("location"),
                    "test.data" to listOf("data"),
                )
            )
        )
        val enrichedEvent = locationEnricher.enrich(event)

        assertEquals(event.name, enrichedEvent.name)
        assertEquals(event.data!![SemanticKeys.LOCATION_NAME], enrichedEvent.data!![SemanticKeys.LOCATION_NAME])
        assertEquals("data", enrichedEvent.data!!["test.data"])
    }

    @Test
    fun testSimpleAddressMatch() {
        val event = createTestEvent()
        val locationEnricher = createTestEnricher(
            listOf(
                mapOf(
                    SemanticKeys.LOCATION_ADDRESS to listOf("address"),
                    "test.data" to listOf("data"),
                )
            )
        )
        val enrichedEvent = locationEnricher.enrich(event)

        assertEquals(event.name, enrichedEvent.name)
        assertEquals(event.data!![SemanticKeys.LOCATION_ADDRESS], enrichedEvent.data!![SemanticKeys.LOCATION_ADDRESS])
        assertEquals("data", enrichedEvent.data!!["test.data"])
    }

    @Test
    fun testNoMatch() {
        val event = createTestEvent()
        val locationEnricher = createTestEnricher(
            listOf(
                mapOf(
                    SemanticKeys.LOCATION_NAME to listOf("location2"),
                    SemanticKeys.LOCATION_ADDRESS to listOf("address2"),
                    "test.data" to listOf("data"),
                )
            )
        )
        val enrichedEvent = locationEnricher.enrich(event)

        assertEquals(event, enrichedEvent)
    }

    @Test
    fun testLocationAliasMatch() {
        val event = createTestEvent()
        val locationEnricher = createTestEnricher(
            listOf(
                mapOf(
                    SemanticKeys.LOCATION_NAME to listOf("location2", "location"),
                    "test.data" to listOf("data"),
                )
            )
        )
        val enrichedEvent = locationEnricher.enrich(event)

        assertEquals(event.name, enrichedEvent.name)
        assertEquals("location2", enrichedEvent.data!![SemanticKeys.LOCATION_NAME])
        assertEquals("data", enrichedEvent.data!!["test.data"])
    }

    private fun createTestEnricher(testData: List<LocationData>): LocationEnricher {
        val locationEnricher = LocationEnricher(object : LocationEnricherUpdater {
            override fun updateData(): List<LocationData> {
                return testData
            }
        })
        locationEnricher.update()
        return locationEnricher
    }

    private fun createNoopEnricher(): LocationEnricher {
        val locationEnricher = LocationEnricher(LocationEnricherNoopUpdater())
        locationEnricher.update()
        return locationEnricher
    }

    private fun createTestEvent(): Event {
        return Event(
            "test", ZonedDateTime.now(),
            mapOf(
                SemanticKeys.LOCATION_NAME to "location",
                SemanticKeys.LOCATION_ADDRESS to "address",
            )
        )
    }
}