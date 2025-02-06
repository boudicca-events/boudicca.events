package base.boudicca.publisher.event.ical

import base.boudicca.SemanticKeys
import base.boudicca.api.search.QueryDTO
import base.boudicca.api.search.SearchClient
import base.boudicca.model.Event
import biweekly.Biweekly
import biweekly.ICalVersion
import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.property.DateEnd
import biweekly.property.DateStart
import biweekly.property.Location
import biweekly.property.Sequence
import biweekly.property.Uid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

@Service
class CalendarService @Autowired constructor(@Value("\${boudicca.search.url}") private val searchUrl: String) {

    private val searchClient = SearchClient(searchUrl)

    fun createCalendar(events: List<Event>): ByteArray {
        // create the calendar
        val calendar = ICalendar()
        calendar.setProductId("-//Boudicca//DE")
        calendar.version = ICalVersion.V2_0

        events.forEach { event ->
            val location = event.data[SemanticKeys.LOCATION_NAME]
            val endDate = parseEndDate(event.data[SemanticKeys.ENDDATE])
            val calendarEvent = createEvent(
                event.name, event.startDate, location, endDate, 0
            )

            calendar.addEvent(calendarEvent)
        }

        return Biweekly.write(calendar).go().toByteArray()
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
        //todo get description?
        val titleHash = title.hashCode()
        val event = VEvent()
        event.setSummary(title)
        event.dateStart = DateStart(Date(startDateTime.toInstant().toEpochMilli()))
        if (endDateTime != null) {
            event.dateEnd = DateEnd(Date(endDateTime.toInstant().toEpochMilli()))
            event.uid = Uid("event-${startDateTime}-${endDateTime}-${titleHash}")
        } else {
            event.uid = Uid("event-${startDateTime}-${titleHash}")
        }

        // set the event properties
        if (location != null) {
            event.location = Location(location)
        }
        event.sequence = Sequence(sequence)

        return event
    }

    fun getEvents(query: String): ByteArray {
        val events = searchClient.queryEvents(QueryDTO(query,0, Int.MAX_VALUE))
        return createCalendar(events.result)
    }
}
