package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * fhLUG: Fachhochschulcampus Hagenberg Linux User Group
 */
class FhLugCollector : TwoStepEventCollector<VEvent>("fhLUG") {

    private val LOG = LoggerFactory.getLogger(this::class.java)

    private val fetcher = Fetcher()
    private val baseUrl = "https://fhlug.at/"
    private val icsUrl = "${baseUrl}events/events.ics"

    // get by city name to account for daylight saving time
    private val viennaZoneId = TimeZone.getTimeZone("Europe/Vienna").toZoneId()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd['T'HHmmssX]")

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
        val eventStartTime = parseStartDate(event)

        return Event(
            eventName,
            eventStartTime,
            buildMap {
                if (event.location != null) {
                    put(SemanticKeys.LOCATION_NAME, event.location.value)
                }
                if (event.description != null) {
                    put(SemanticKeys.DESCRIPTION, event.description.value)
                }
                put(SemanticKeys.TAGS, listOf("fhLUG", "Linux", "User Group", "Free Software").toString())
                val url = getUrl(event)
                if (url != null) {
                    put(SemanticKeys.URL, url)
                }
                put(SemanticKeys.TYPE, "techmeetup") // TODO same as with Technologieplauscherl
                put(SemanticKeys.CATEGORY, EventCategory.TECH.name)
                put(SemanticKeys.REGISTRATION, "free")
                put("url.ics", icsUrl)
                put("fhlug.uid", event.uid.value)
                put(SemanticKeys.SOURCES, "${icsUrl}\n${baseUrl}")
            }
        )
    }

    private fun getUrl(event: VEvent): String? {
        if (event.url != null) {
            return event.url.value
        }

        val description = event.description?.value ?: ""
        val document = Jsoup.parse(description)
        val href = document.select("a").first()?.attr("href")
        if (href?.startsWith("http") == true) {
            return href
        }

        if (description.startsWith("http")) {
            return description
        }

        return null
    }

    private fun parseStartDate(event: VEvent): OffsetDateTime {
        return when (val startDate = dateTimeFormatter.parseBest(event.startDate.value, OffsetDateTime::from, LocalDate::from)) {
            is OffsetDateTime -> startDate
            is LocalDate -> startDate.atStartOfDay().atZone(viennaZoneId).toOffsetDateTime()
            else -> throw IllegalArgumentException("Unsupported temporal accessor")
        }
    }
}
