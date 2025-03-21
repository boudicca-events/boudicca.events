package base.boudicca.model

import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import base.boudicca.SemanticKeys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class EventEntryConversionTest {
    @Test
    fun testSimpleEventToEntry() {
        val now = OffsetDateTime.now()
        val entry =
            Event.toEntry(Event("my event", now, mapOf("otherProperty" to "value", "otherProperty:lang=de" to "wert")))

        assertEquals(4, entry.size)
        assertEquals("my event", entry[SemanticKeys.NAME])
        assertEquals(DateTimeFormatter.ISO_DATE_TIME.format(now), entry[SemanticKeys.STARTDATE + ":format=date"])
        assertEquals("value", entry["otherProperty"])
        assertEquals("wert", entry["otherProperty:lang=de"])
    }

    @Test
    fun testEntryToEventSuccess() {
        val entry = mapOf(
            "name" to "My Event",
            "startDate" to "2023-04-27T23:59:00+02:00",
            "startDate:format=date" to "2024-04-27T23:59:00+02:00",
            "pictureUrl" to "https://i.insider.com/2b37544bfe5eb549a8378b00?width=1024",
            "description" to "my default lang description",
            "description:lang=de" to "meine deutsche beschreibung",
            "description:lang=en" to "my english description",
            "description:format=markdown:lang=en" to "#my english markdown description"
        )

        val event = Event.fromEntry(entry)

        assertThat(event).isNotNull()
        assertEquals(6, event!!.data.size)
        assertEquals("My Event", event.name)
        assertEquals(
            OffsetDateTime.parse("2024-04-27T23:59:00+02:00", DateTimeFormatter.ISO_DATE_TIME),
            event.startDate
        )
        assertEquals(event.data["description"], "my default lang description")
    }

    @Test
    fun testEntryToEventStartDateFallback() {
        val entry = mapOf(
            "name" to "My Event",
            "startDate" to "2024-04-27T23:59:00+02:00",
            "pictureUrl" to "https://i.insider.com/2b37544bfe5eb549a8378b00?width=1024",
            "description" to "my default lang description",
            "description:lang=de" to "meine deutsche beschreibung",
            "description:lang=en" to "my english description",
            "description:format=markdown:lang=en" to "#my english markdown description"
        )

        val event = Event.fromEntry(entry)

        assertThat(event).isNotNull()
        assertEquals(5, event!!.data.size)
        assertEquals("My Event", event.name)
        assertEquals(
            OffsetDateTime.parse("2024-04-27T23:59:00+02:00", DateTimeFormatter.ISO_DATE_TIME),
            event.startDate
        )
        assertEquals(event.data["description"], "my default lang description")
    }

    @Test
    fun testEntryToEventNoName() {
        val entry = mapOf(
            "startDate" to "2024-04-27T23:59:00+02:00",
            "description" to "my default lang description",
        )

        val event = Event.fromEntry(entry)

        assertThat(event).isNull()
    }

    @Test
    fun testEntryToEventNoStartDate() {
        val entry = mapOf(
            "name" to "My Event",
            "description" to "my default lang description",
        )

        val event = Event.fromEntry(entry)

        assertThat(event).isNull()
    }

    @Test
    fun testEntryToEventNameVariant() {
        val entry = mapOf(
            "name:lang=en" to "My Event",
            "startDate" to "2024-04-27T23:59:00+02:00",
            "description" to "my default lang description",
        )

        val event = Event.fromEntry(entry)

        assertThat(event).isNotNull()
        assertEquals("My Event", event!!.name)
    }
}
