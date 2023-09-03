package events.boudicca.api.eventcollector

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.collections.Collections
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.ApiException
import events.boudicca.openapi.api.EventIngestionResourceApi
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

class EventCollectorScheduler(
    private val interval: Duration = Duration.ofHours(24),
    boudiccaUrl: String = autoDetectUrl()
) {
    private val eventCollectors: MutableList<EventCollector> = mutableListOf()
    private val ingestionApi: EventIngestionResourceApi
    private val LOG = LoggerFactory.getLogger(this::class.java)

    init {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(boudiccaUrl)
        apiClient.setRequestInterceptor {
            it.header(
                "Authorization",
                "Basic " + Base64.getEncoder().encodeToString(autoDetectBasicAuth().encodeToByteArray())
            )
        }
        ingestionApi = EventIngestionResourceApi(apiClient)
    }

    fun addEventCollector(eventCollector: EventCollector): EventCollectorScheduler {
        eventCollectors.add(eventCollector)
        return this
    }

    fun startWebUi(port: Int = 8083): EventCollectorScheduler {
        EventCollectorWebUi(port, this).start()

        return this
    }

    fun run(): Nothing {
        while (true) {
            runOnce()
            LOG.info("all event collectors ran, sleeping for $interval")
            Thread.sleep(interval.toMillis())
        }
    }

    fun runOnce() {
        Collections.startFullCollection()
        try {
            eventCollectors
                .parallelStream()
                .forEach { collect(it) }
        } finally {
            Collections.endFullCollection()
        }
    }

    private fun collect(eventCollector: EventCollector) {
        Collections.startSingleCollection(eventCollector)
        try {
            val events = eventCollector.collectEvents()
            Collections.getCurrentSingleCollection()!!.totalEventsCollected = events.size
            if (events.isEmpty()) {
                LOG.warn("collector collected 0 events!")
            }
            try {
                for (event in events) {
                    ingestionApi.ingestAddPost(mapToApiEvent(postProcess(event, eventCollector.getName())))
                }
            } catch (e: ApiException) {
                LOG.error("could not ingest events, is the core available?", e)
            }
        } catch (e: Exception) {
            LOG.error("collector threw exception while collecting", e)
        } finally {
            Collections.endSingleCollection()
        }
    }

    private fun postProcess(event: Event, collectorName: String): Event {
        if (!event.additionalData.containsKey(SemanticKeys.COLLECTORNAME)) {
            return Event(
                event.name,
                event.startDate,
                event.additionalData.toMutableMap().apply { put(SemanticKeys.COLLECTORNAME, collectorName) }
            )
        }
        if (event.name.isBlank()) {
            LOG.warn("event from collector $collectorName has empty name: $event")
        }
        for (entry in event.additionalData.entries) {
            if (entry.value.isBlank()) {
                LOG.warn("event from collector $collectorName contains empty field ${entry.key}: $event")
            }
        }
        return event
    }

    private fun mapToApiEvent(event: Event): events.boudicca.openapi.model.Event {
        return events.boudicca.openapi.model.Event()
            .name(event.name)
            .startDate(event.startDate)
            .data(event.additionalData)
    }

    fun getCollectors(): List<EventCollector> {
        return eventCollectors
    }

}

private fun autoDetectUrl(): String {
    var url = System.getenv("BOUDICCA_URL")
    if (url != null && url.isNotBlank()) {
        return url
    }
    url = System.getProperty("boudiccaUrl")
    if (url != null && url.isNotBlank()) {
        return url
    }
    return "http://localhost:8081"
}

private fun autoDetectBasicAuth(): String {
    var auth = System.getenv("BOUDICCA_AUTH")
    if (auth != null && auth.isNotBlank()) {
        return auth
    }
    auth = System.getProperty("boudiccaAuth")
    if (auth != null && auth.isNotBlank()) {
        return auth
    }
    return "ingest:ingest"
}
