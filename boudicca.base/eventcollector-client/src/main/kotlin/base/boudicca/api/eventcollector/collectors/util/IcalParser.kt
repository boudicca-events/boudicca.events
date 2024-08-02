package base.boudicca.api.eventcollector.collectors.util

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import biweekly.Biweekly
import biweekly.component.VEvent
import biweekly.property.DateEnd
import biweekly.property.DateStart
import biweekly.util.ICalDate
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * utility class for parsing and mapping ical resources to VEvents and then to Events
 */
object IcalParser {

    private val LOG = LoggerFactory.getLogger(this::class.java)

    /**
     * parses an icalResource (aka the string contents of a .ics file) to vEvents and maps them to Events
     * @param icalResource the ics file to parse and map
     */
    fun parseAndMapToEvents(icalResource: String): List<Event> {
        val vEvents = parseToVEvents(icalResource)
        return mapVEventsToEvents(vEvents)
    }

    /**
     * parses an icalResource (aka the string contents of a .ics file) to vEvents
     * @param icalResource the ics file to parse and map
     */
    fun parseToVEvents(icalResource: String): List<VEvent> {
        val allCalendars = Biweekly.parse(icalResource).all()
        val vEvents = allCalendars
            .flatMap { it.events }
        return vEvents
    }

    /**
     * maps a collection of vEvents to Events
     */
    fun mapVEventsToEvents(vEvents: List<VEvent>): List<Event> {
        return vEvents
            .map { mapVEventToEvent(it) } //map to optional events
            .filter { it.isPresent } //filter only successful ones
            .map { it.get() }
    }

    /**
     * maps a single vEvent to an Event. returns an optional which is empty when the vEvent does not include the required data for creating an Event
     */
    fun mapVEventToEvent(vEvent: VEvent): Optional<Event> {
        if (vEvent.dateStart == null) {
            LOG.warn("event with uid ${vEvent.uid} and url ${vEvent.url} has no startDate!")
            return Optional.empty()
        }

        val name = vEvent.summary.value
        val startDate = getStartDate(vEvent.dateStart)

        val data = mutableMapOf<String, String>()
        if (vEvent.location != null) {
            data[SemanticKeys.LOCATION_NAME] = vEvent.location.value
        }
        if (vEvent.description != null) {
            data[SemanticKeys.DESCRIPTION] = vEvent.description.value
        }
        if (vEvent.url != null) {
            data[SemanticKeys.URL] = vEvent.url.value
        }
        if (vEvent.uid != null) {
            data["ics.event.uid"] = vEvent.uid.value
        }
        if (vEvent.dateEnd != null) {
            data[SemanticKeys.ENDDATE] = getEndDate(vEvent.dateEnd)
        }

        return Optional.of(Event(name, startDate, data))
    }

    private fun getEndDate(dateEnd: DateEnd): String {
        return DateTimeFormatter.ISO_DATE_TIME.format(getDate(dateEnd.value))
    }

    private fun getStartDate(dateStart: DateStart): OffsetDateTime {
        return getDate(dateStart.value)
    }

    private fun getDate(iCalDate: ICalDate): OffsetDateTime {
        return iCalDate.toInstant().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }
}