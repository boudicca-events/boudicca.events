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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * VorAlpen Linux User Group
 */
class ValugCollector : TwoStepEventCollector<VEvent>("valug") {

    private val LOG = LoggerFactory.getLogger(this::class.java)

    private val fetcher = Fetcher()
    private val baseUrl = "https://valug.at/"
    private val icsUrl = "${baseUrl}events/index.ics"

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
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        // get by city name to account for daylight saving time
        val viennaZoneId = TimeZone.getTimeZone("Europe/Vienna").toZoneId()
        val eventStartDate = LocalDateTime.parse(event.startDate.value, formatter)
            .atZone(viennaZoneId).toOffsetDateTime()

        return Event(
            eventName, eventStartDate,
            buildMap {
                if (event.location != null) {
                    put(SemanticKeys.LOCATION_NAME, event.location.value)
                }
                if (event.description != null) {
                    put(SemanticKeys.DESCRIPTION, event.description.value)
                }
                put(SemanticKeys.TAGS, listOf("VALUG", "Linux", "User Group").toString())
                put(SemanticKeys.URL, event.url.value)
                put(SemanticKeys.TYPE, "techmeetup") // TODO same as with Technologieplauscherl
                put(SemanticKeys.CATEGORY, EventCategory.TECH.name)
                put(SemanticKeys.REGISTRATION, "free")
                put("url.ics", icsUrl)
                put("valug.uid", event.uid.value)
                put(SemanticKeys.SOURCES, "${icsUrl}\n${baseUrl}")
            }
        )
    }
}
