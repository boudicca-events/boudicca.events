package at.cnoize.boudicca

import at.cnoize.boudicca.api.ComplexSearchDto
import at.cnoize.boudicca.api.Event
import at.cnoize.boudicca.api.EventApi
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.TimeZoneRegistryFactory
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class CalendarService {

    fun createCalendar(events: Set<Event>): File {
        // create the calendar
        val calendar = Calendar()

        calendar.properties.add(ProdId("-//Boudicca//DE"))
        calendar.properties.add(Version.VERSION_2_0)

        events.forEach { event ->
            val location = event.data?.get("start.location.name")
            val calendarEvent = createEvent(
                event.name,
                event.startDate,
                location,
                null,
                0
            )

            calendar.components.add(calendarEvent)
        }

        val file = File("calendar.ics")
        file.writeText(calendar.toString())

        return file
    }

    fun createEvent(
        title: String,
        startDateTime: OffsetDateTime,
        location: String?,
        endDateTime: OffsetDateTime?,
        sequence: Int,
    ): VEvent {
        // create the event
        val event = if (endDateTime == null) {
            VEvent(
                DateTime(startDateTime.toInstant().toEpochMilli()),
                title
            ).also {
                it.properties.add(Uid("event-${startDateTime}"))
            }
        } else {
            VEvent(
                DateTime(startDateTime.toInstant().toEpochMilli()),
                DateTime(endDateTime.toInstant().toEpochMilli()),
                title
            ).also {
                it.properties.add(Uid("event-${startDateTime}-${endDateTime}"))
            }
        }

        // set the event properties
        if (location != null) {
            event.properties.add(Location(location))
        }
        event.properties.add(Sequence(sequence))

        return event
    }

    fun getEvents(labels: List<String>?): File {
        val eventApi = EventApi()
        val events = if (labels == null) {
            eventApi.list()
        } else {
            val key = "tags"
            val searchPairs = labels.map { Pair(key, it) }.toSet()

            eventApi.searchBy(ComplexSearchDto(anyValueForKeyContains = searchPairs))
        }

        return createCalendar(events)
    }
}