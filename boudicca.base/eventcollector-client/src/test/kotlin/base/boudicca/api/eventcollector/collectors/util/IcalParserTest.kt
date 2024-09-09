package base.boudicca.api.eventcollector.collectors.util

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import biweekly.component.VEvent
import biweekly.property.DateStart
import biweekly.property.Summary
import biweekly.property.Uid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class IcalParserTest {

    @Test
    fun testEmptyIcal() {
        assertTrue(IcalParser.parseAndMapToEvents("").isEmpty())
    }

    @Test
    fun testInvalidIcal() {
        assertTrue(IcalParser.parseAndMapToEvents("THIS IS NOT AN ICAL FILE").isEmpty())
    }

    @Test
    fun testSimpleIcsFile() {
        val events = loadAndParseAndMapEvents("test1.ics")

        assertEquals(1, events.size)
        val event = events[0]
        val data = event.data
        assertEquals("No title", event.name)
        assertEquals(OffsetDateTime.of(2007, 12, 14, 1, 0, 0, 0, ZoneOffset.ofHours(1)), event.startDate)
        assertEquals(
            "a840b839819203073326e820176eb4ba757cc96cca71f43f8d34946a917dafe6@events.valug.at",
            data["ics.event.uid"]
        )
        assertEquals("https://valug.at/events/2007-12-14/", data[SemanticKeys.URL])
        assertFalse(data.containsKey(SemanticKeys.DESCRIPTION))
        assertFalse(data.containsKey(SemanticKeys.ENDDATE))
    }

    @Test
    fun testSecondSimpleIcsFile() {
        val events = loadAndParseAndMapEvents("test2.ics")

        assertEquals(1, events.size)
        val event = events[0]
        val data = event.data
        assertEquals("Other title", event.name)
        assertEquals(OffsetDateTime.of(2007, 12, 14, 1, 0, 0, 0, ZoneOffset.ofHours(1)), event.startDate)
        assertEquals("2007-12-14T01:10:00+01:00", data[SemanticKeys.ENDDATE + ":format=date"])
        assertEquals(
            "a840b839819203073326e820176eb4ba757cc96cca71f43f8d34946a917dafe6@events.valug.at",
            data["ics.event.uid"]
        )
        assertEquals("https://valug.at/events/2007-12-14/", data[SemanticKeys.URL])
        assertEquals("Some description", data[SemanticKeys.DESCRIPTION])
    }

    @Test
    fun testMultipleEvents() {
        val events = loadAndParseAndMapEvents("test_multiple_events.ics")

        assertEquals(2, events.size)
        assertEquals("event1", events[0].name)
        assertEquals("event2", events[1].name)
    }

    @Test
    fun testMultipleCalendars() {
        val events = loadAndParseAndMapEvents("test_multiple_calendars.ics")

        assertEquals(4, events.size)
        assertEquals("event1_1", events[0].name)
        assertEquals("event1_2", events[1].name)
        assertEquals("event2_1", events[2].name)
        assertEquals("event2_2", events[3].name)
    }

    @Test
    fun testParseIcalResource() {
        val vEvents = IcalParser.parseToVEvents(loadTestData("test1.ics"))

        assertEquals(1, vEvents.size)
        assertEquals(
            "a840b839819203073326e820176eb4ba757cc96cca71f43f8d34946a917dafe6@events.valug.at",
            vEvents[0].uid.value
        )
    }

    @Test
    fun testMapVEventToEvent() {
        val now = ZonedDateTime.now(ZoneId.of("Europe/Vienna")).withNano(0) //conversion to date does not keep nanos
            .toOffsetDateTime()
        val vEvent = VEvent()
        vEvent.uid = Uid("myUid")
        vEvent.summary = Summary("mySummary")
        vEvent.dateStart = DateStart(Date(now.toInstant().toEpochMilli()))

        val event = mapVEventToEvent(vEvent)

        assertEquals("myUid", event.data["ics.event.uid"])
        assertEquals("mySummary", event.name)
        assertEquals(now, event.startDate)
    }

    @Test
    fun testMapInvalidVEventToEvent() {
        val vEvent = VEvent()

        val event = tryMapVEventToEvent(vEvent)

        assertFalse(event.isPresent)
    }

    @Test
    fun testMapVEventsToEvent() {
        val now = OffsetDateTime.now().withNano(0) //conversion to date does not keep nanos
        val vEvent = VEvent()
        vEvent.uid = Uid("myUid")
        vEvent.summary = Summary("mySummary")
        vEvent.dateStart = DateStart(Date(now.toInstant().toEpochMilli()))

        val invalidVEvent = VEvent()
        val vEvents = listOf(vEvent, vEvent, invalidVEvent)

        val events = IcalParser.mapVEventsToEvents(vEvents)

        assertEquals(2, events.size)
    }

    private fun mapVEventToEvent(vEvent: VEvent): Event {
        return IcalParser.mapVEventToEvent(vEvent).get().toFlatEvent()
    }

    private fun tryMapVEventToEvent(vEvent: VEvent): Optional<Event> {
        return IcalParser.mapVEventToEvent(vEvent).map { it.toFlatEvent() }
    }

    private fun loadAndParseAndMapEvents(testFile: String): List<Event> {
        return IcalParser.parseAndMapToEvents(loadTestData(testFile)).map { it.toFlatEvent() }
    }

    private fun loadTestData(testFile: String) =
        String(this.javaClass.getResourceAsStream("/ical/$testFile").readAllBytes())
}