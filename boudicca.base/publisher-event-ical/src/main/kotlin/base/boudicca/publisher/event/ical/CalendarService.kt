package base.boudicca.publisher.event.ical

import base.boudicca.model.Event
import base.boudicca.SemanticKeys
import base.boudicca.api.search.QueryDTO
import base.boudicca.api.search.SearchClient
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class CalendarService @Autowired constructor(@Value("\${boudicca.search.url}") private val searchUrl: String) {

    private val searchClient = SearchClient(searchUrl)

    fun createCalendar(events: List<Event>): ByteArray {
        // create the calendar
        val calendar = Calendar()

        calendar.properties.add(ProdId("-//Boudicca//DE"))
        calendar.properties.add(Version.VERSION_2_0)

        events.forEach { event ->
            val location = event.data[SemanticKeys.LOCATION_NAME]
            val endDate = parseEndDate(event.data[SemanticKeys.ENDDATE])
            val calendarEvent = createEvent(
                event.name, event.startDate, location, endDate, 0
            )

            calendar.components.add(calendarEvent)
        }

        return calendar.toString().toByteArray()
    }

    private fun parseEndDate(endDate: String?): OffsetDateTime? {
        if (endDate == null) {
            return null
        }

        return OffsetDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME)
    }

    fun createEvent(
        title: String,
        startDateTime: OffsetDateTime,
        location: String?,
        endDateTime: OffsetDateTime?,
        sequence: Int,
    ): VEvent {
        val titleHash = title.hashCode()
        // create the event
        val event = if (endDateTime == null) {
            VEvent(
                DateTime(startDateTime.toInstant().toEpochMilli()), title
            ).also {
                it.properties.add(Uid("event-${startDateTime}-${titleHash}"))
            }
        } else {
            VEvent(
                DateTime(startDateTime.toInstant().toEpochMilli()),
                DateTime(endDateTime.toInstant().toEpochMilli()),
                title
            ).also {
                it.properties.add(Uid("event-${startDateTime}-${endDateTime}-${titleHash}"))
            }
        }

        // set the event properties
        if (location != null) {
            event.properties.add(Location(location))
        }
        event.properties.add(Sequence(sequence))

        return event
    }

    fun getEvents(query: String): ByteArray {
        val events = searchClient.queryEvents(QueryDTO(query, 0, Int.MAX_VALUE))
        return createCalendar(events.result)
    }
}