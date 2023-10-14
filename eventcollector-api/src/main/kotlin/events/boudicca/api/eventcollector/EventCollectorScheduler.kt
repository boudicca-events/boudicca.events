package events.boudicca.api.eventcollector

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.collections.Collections
import events.boudicca.enricher.openapi.api.EnricherControllerApi
import events.boudicca.enricher.openapi.model.EnrichRequestDTO
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.ApiException
import events.boudicca.openapi.api.EventIngestionResourceApi
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

class EventCollectorScheduler(
    private val interval: Duration = Duration.ofHours(24),
    private val eventSink: Consumer<Event> = createBoudiccaEventSink(Configuration.getProperty("boudicca.eventdb.url")),
    private val enricherFunction: Function<List<Event>, List<Event>>? =
        createBoudiccaEnricherFunction(Configuration.getProperty("boudicca.enricher.url"))
) : AutoCloseable {

    constructor(
        interval: Duration = Duration.ofHours(24),
        eventDbUrl: String,
    ) : this(interval, createBoudiccaEventSink(eventDbUrl), null)

    constructor(
        interval: Duration = Duration.ofHours(24),
        eventDbUrl: String,
        enricherUrl: String?
    ) : this(interval, createBoudiccaEventSink(eventDbUrl), createBoudiccaEnricherFunction(enricherUrl))

    private val eventCollectors: MutableList<EventCollector> = mutableListOf()
    private val LOG = LoggerFactory.getLogger(this::class.java)
    private var eventCollectorWebUi: EventCollectorWebUi? = null

    fun addEventCollector(eventCollector: EventCollector): EventCollectorScheduler {
        eventCollectors.add(eventCollector)
        return this
    }

    fun startWebUi(port: Int = -1): EventCollectorScheduler {
        val realPort = if (port == -1) {
            Configuration.getProperty("server.port")?.toInt()
                ?: throw IllegalStateException("you need to specify the server.port property!")
        } else {
            port
        }
        synchronized(this) {
            if (eventCollectorWebUi == null) {
                eventCollectorWebUi = EventCollectorWebUi(realPort, this)
                eventCollectorWebUi!!.start()
            }
        }

        return this
    }

    fun run(): Nothing {
        while (true) {
            runOnce()
            LOG.info("sleeping for $interval")
            Thread.sleep(interval.toMillis())
        }
    }

    fun runOnce() {
        LOG.info("starting new full collection")
        Collections.startFullCollection()
        try {
            eventCollectors
                .parallelStream()
                .forEach { collect(it) }
        } finally {
            Collections.endFullCollection()
        }
        LOG.info("full collection done")
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
                val postProcessedEvents = events.map { postProcess(it, eventCollector.getName()) }
                val enrichedEvents = enrich(postProcessedEvents)
                for (event in enrichedEvents) {
                    eventSink.accept(event)
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

    private fun enrich(events: List<Event>): List<Event> {
        return try {
            enricherFunction?.apply(events) ?: events
        } catch (e: Exception) {
            LOG.error("enricher threw exception, submitting events unenriched", e)
            events
        }
    }

    private fun postProcess(event: Event, collectorName: String): Event {
        if (event.name.isBlank()) {
            LOG.warn("event has empty name: $event")
        }
        for (entry in event.additionalData.entries) {
            if (entry.value.isBlank()) {
                LOG.warn("event contains empty field ${entry.key}: $event")
            }
        }
        if (!event.additionalData.containsKey(SemanticKeys.COLLECTORNAME)) {
            return Event(
                event.name,
                event.startDate,
                event.additionalData.toMutableMap().apply { put(SemanticKeys.COLLECTORNAME, collectorName) }
            )
        }
        return event
    }

    fun getCollectors(): List<EventCollector> {
        return eventCollectors
    }

    override fun close() {
        synchronized(this) {
            if (eventCollectorWebUi != null) {
                eventCollectorWebUi!!.stop()
            }
        }
    }

}

fun createBoudiccaEventSink(eventDbUrl: String?): Consumer<Event> {
    if (eventDbUrl.isNullOrBlank()) {
        throw IllegalStateException("you need to specify the boudicca.eventdb.url property!")
    }
    val apiClient = ApiClient()
    apiClient.updateBaseUri(eventDbUrl)
    apiClient.setRequestInterceptor {
        it.header(
            "Authorization",
            "Basic " + Base64.getEncoder()
                .encodeToString(
                    Configuration.getProperty("boudicca.ingest.auth")?.encodeToByteArray()
                        ?: throw IllegalStateException("you need to specify the boudicca.ingest.auth property!")
                )
        )
    }
    val ingestionApi = EventIngestionResourceApi(apiClient)
    return Consumer {
        ingestionApi.ingestAddPost(mapToApiEvent(it))
    }
}

fun createBoudiccaEnricherFunction(enricherUrl: String?): Function<List<Event>, List<Event>>? {
    if (enricherUrl.isNullOrBlank()) {
        return null
    }
    val apiClient = events.boudicca.enricher.openapi.ApiClient()
    apiClient.updateBaseUri(enricherUrl)
    val enricherApi = EnricherControllerApi(apiClient)
    return Function<List<Event>, List<Event>> { events ->
        enricherApi.enrich(
            EnrichRequestDTO().events(events.map { mapToEnricherEvent(it) })
        ).map { mapToEventCollectorEvent(it) }
    }
}

private fun mapToApiEvent(event: Event): events.boudicca.openapi.model.Event {
    return events.boudicca.openapi.model.Event()
        .name(event.name)
        .startDate(event.startDate)
        .data(event.additionalData)
}

private fun mapToEnricherEvent(event: Event): events.boudicca.enricher.openapi.model.Event {
    return events.boudicca.enricher.openapi.model.Event()
        .name(event.name)
        .startDate(event.startDate)
        .data(event.additionalData)
}

private fun mapToEventCollectorEvent(event: events.boudicca.enricher.openapi.model.Event): Event {
    return Event(event.name, event.startDate, event.data ?: emptyMap())
}