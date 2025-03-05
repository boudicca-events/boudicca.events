package base.boudicca.api.eventcollector.collectors.util

import base.boudicca.SemanticKeys
import base.boudicca.TextProperty
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import biweekly.Biweekly
import biweekly.component.VEvent
import biweekly.property.DateEnd
import biweekly.property.DateStart
import biweekly.util.ICalDate
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

/**
 * utility class for parsing and mapping ical resources to VEvents and then to Events
 */
object IcalParser {

    private val logger = KotlinLogging.logger {}

    /**
     * parses an icalResource (aka the string contents of a .ics file) to vEvents and maps them to Events
     * @param icalResource the ics file to parse and map
     */
    fun parseAndMapToEvents(icalResource: String): List<StructuredEvent> {
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
    fun mapVEventsToEvents(vEvents: List<VEvent>): List<StructuredEvent> {
        return vEvents
            .map { mapVEventToEvent(it) } //map to optional events
            .filter { it.isPresent } //filter only successful ones
            .map { it.get() }
    }

    /**
     * maps a single vEvent to an Event. returns an optional which is empty when the vEvent does not include the required data for creating an Event
     */
    fun mapVEventToEvent(vEvent: VEvent): Optional<StructuredEvent> {
        if (vEvent.dateStart == null) {
            logger.warn { "event with uid ${vEvent.uid} and url ${vEvent.url} has no startDate!" }
            return Optional.empty()
        }

        val name = vEvent.summary.value
        val startDate = getStartDate(vEvent.dateStart)

        val event = structuredEvent(name, startDate) {
            if (vEvent.location != null) {
                withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, vEvent.location.value)
            }
            if (vEvent.description != null) {
                withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, vEvent.description.value)
            }
            if (vEvent.url != null) {
                withProperty(SemanticKeys.URL_PROPERTY, URI.create(vEvent.url.value))
            }
            if (vEvent.uid != null) {
                withProperty(TextProperty("ics.event.uid"), vEvent.uid.value)
            }
            if (vEvent.dateEnd != null) {
                withProperty(SemanticKeys.ENDDATE_PROPERTY, getEndDate(vEvent.dateEnd))
            }
        }

        return Optional.of(event)
    }

    private fun getEndDate(dateEnd: DateEnd): OffsetDateTime {
        return getDate(dateEnd.value)
    }

    private fun getStartDate(dateStart: DateStart): OffsetDateTime {
        return getDate(dateStart.value)
    }

    private fun getDate(iCalDate: ICalDate): OffsetDateTime {
        return iCalDate.toInstant().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }
}
