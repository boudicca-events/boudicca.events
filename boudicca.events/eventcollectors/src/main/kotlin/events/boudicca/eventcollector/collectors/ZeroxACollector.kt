package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class ZeroxACollector : TwoStepEventCollector<VEvent>("ZeroxA") {

    private val LOG = LoggerFactory.getLogger(this::class.java)

    private val fetcher = Fetcher()
    private val baseUrl = "https://0xa.at/"
    private val icsUrl = "${baseUrl}events.ics"

    override fun getAllUnparsedEvents(): List<VEvent> {
        val feed = fetcher.fetchUrl(icsUrl)
        val builder = CalendarBuilder()
        val calendar: Calendar = builder.build(StringReader(feed))
        return calendar.components.filterIsInstance<VEvent>()
    }

    override fun parseEvent(event: VEvent): Event? {
        if (event.startDate == null) {
            LOG.warn("event with uid ${event.uid.value} and url ${event.url?.value} has no startDate!")
            return null
        }
        val eventName = event.summary.value
        val eventStartDate = parseDate(event)

        return Event(
            eventName, eventStartDate,
            buildMap {
                if (event.location != null) {
                    put(SemanticKeys.LOCATION_NAME, event.location.value)
                }
                if (event.description != null) {
                    put(SemanticKeys.DESCRIPTION, event.description.value)
                }
                put(SemanticKeys.TAGS, listOf("0xA", "Science", "Association").toString())
                put(SemanticKeys.URL, event.url.value)
                put(SemanticKeys.TYPE, "techmeetup") // TODO same as with Technologieplauscherl
                put(SemanticKeys.CATEGORY, EventCategory.TECH.name)
                put(SemanticKeys.REGISTRATION, "free")
                put("url.ics", icsUrl)
                put("0xa.uid", event.uid.value)
                put(SemanticKeys.SOURCES, "${icsUrl}\n${baseUrl}")
            }
        )
    }

    private fun parseDate(event: VEvent): OffsetDateTime {
        val formatterWithTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        val formatterWithoutTime = DateTimeFormatter.ofPattern("yyyyMMdd")

        val viennaZoneId = TimeZone.getTimeZone("Europe/Vienna").toZoneId()

        return try {
            // Try parsing with the datetime formatter
            LocalDateTime.parse(event.startDate.value, formatterWithTime)
                .atZone(viennaZoneId).toOffsetDateTime()
        } catch (e: DateTimeParseException) {
            // If it fails, try parsing with the date-only formatter
            LocalDate.parse(event.startDate.value, formatterWithoutTime)
                .atStartOfDay(viennaZoneId).toOffsetDateTime()
        }
    }
}
