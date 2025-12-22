package base.boudicca.publisher.event.ical

import base.boudicca.SemanticKeys
import base.boudicca.api.search.QueryDTO
import base.boudicca.api.search.SearchClient
import base.boudicca.model.Event
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEvent
import biweekly.Biweekly
import biweekly.ICalVersion
import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.property.DateEnd
import biweekly.property.DateStart
import biweekly.property.Description
import biweekly.property.Location
import biweekly.property.Uid
import biweekly.property.Url
import io.opentelemetry.api.OpenTelemetry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

@Service
class CalendarService
    @Autowired
    constructor(
        @Value("\${boudicca.search.url}") private val searchUrl: String,
        otel: OpenTelemetry,
    ) {
        private val searchClient = SearchClient(searchUrl, otel)

        fun createCalendar(events: List<Event>): ByteArray {
            // create the calendar
            val calendar = ICalendar()
            calendar.setProductId("-//Boudicca//DE")
            calendar.version = ICalVersion.V2_0

            events.forEach { event ->
                calendar.addEvent(createEvent(event.toStructuredEvent()))
            }

            return Biweekly.write(calendar).go().toByteArray()
        }

        fun createEvent(event: StructuredEvent): VEvent {
            val vEvent = VEvent()
            vEvent.setSummary(event.name)
            vEvent.dateStart = DateStart(event.startDate.toDate())
            vEvent.uid = Uid("event-${event.startDate}-${event.name}")

            val endDate = event.getProperty(SemanticKeys.ENDDATE_PROPERTY).firstOrNull()
            val locationName = event.getProperty(SemanticKeys.LOCATION_NAME_PROPERTY).firstOrNull()
            val locationUrl = event.getProperty(SemanticKeys.LOCATION_URL_PROPERTY).firstOrNull()
            val description = event.getProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY).firstOrNull()
            val url = event.getProperty(SemanticKeys.URL_PROPERTY).firstOrNull()

            endDate?.let { (_, endDate) ->
                vEvent.dateEnd = DateEnd(endDate.toDate())
            }
            buildTextWithUrlSuffix(locationName, locationUrl)?.let {
                vEvent.location = Location(it)
            }
            buildTextWithUrlSuffix(description, url)?.let {
                vEvent.description = Description(it)
            }
            url?.let { (_, url) ->
                vEvent.url = Url(url.toString())
            }

            return vEvent
        }

        private fun buildTextWithUrlSuffix(
            text: Pair<Key, String>?,
            url: Pair<Key, URI>?,
        ): String? {
            val textText = text?.second
            val urlText = url?.second?.toString()
            return if (!textText.isNullOrBlank() && !urlText.isNullOrBlank()) {
                "$textText ($urlText)"
            } else {
                return textText ?: urlText
            }
        }

        fun getEvents(query: String): ByteArray {
            val events = searchClient.queryEvents(QueryDTO(query, 0, Int.MAX_VALUE))
            return createCalendar(events.result)
        }
    }

private fun OffsetDateTime.toDate() = Date.from(this.toInstant())
