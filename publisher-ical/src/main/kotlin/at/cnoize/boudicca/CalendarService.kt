package at.cnoize.boudicca

import at.cnoize.boudicca.model.ComplexSearchDto
import at.cnoize.boudicca.model.Event
import at.cnoize.boudicca.publisherapi.PublisherApi
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.io.File
import java.time.ZonedDateTime
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.Path

@RegisterRestClient(configKey = "ingestion-api")
@ApplicationScoped
@Path("/events")
interface CalendarApi : PublisherApi

@ApplicationScoped
class CalendarService @Inject constructor(@RestClient private val calendarApi: CalendarApi) {

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
            startDateTime: ZonedDateTime,
            location: String?,
            endDateTime: ZonedDateTime?,
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
        val events = if (labels == null) {
            calendarApi.list()
        } else {
            val key = "tags"
            val searchPairs = labels.map { listOf(key, it) }.toSet()

            calendarApi.searchBy(ComplexSearchDto(anyValueForKeyContains = searchPairs))
        }

        return createCalendar(events)
    }
}