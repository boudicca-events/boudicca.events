package base.boudicca.api.eventcollector

import base.boudicca.Event
import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventdb.ingest.EventDB
import events.boudicca.enricher.openapi.api.EnricherControllerApi
import events.boudicca.enricher.openapi.model.EnrichRequestDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.Executors
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
    private val executor = Executors.newCachedThreadPool { Thread(it).apply { isDaemon = true } }

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
                .map {
                    executor.submit { collect(it) }
                }
                .forEach { it.get() }
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
                    retry(LOG) {
                        eventSink.accept(event)
                    }
                }
            } catch (e: RuntimeException) {
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
            retry(LOG) {
                enricherFunction?.apply(events) ?: events
            }
        } catch (e: Exception) {
            LOG.error("enricher threw exception, submitting events un-enriched", e)
            events
        }
    }

    private fun postProcess(event: Event, collectorName: String): Event {
        if (event.name.isBlank()) {
            LOG.warn("event has empty name: $event")
        }
        for (entry in event.data.entries) {
            if (entry.value.isBlank()) {
                LOG.warn("event contains empty field ${entry.key}: $event")
            }
        }
        if (!event.data.containsKey(SemanticKeys.COLLECTORNAME)) {
            return Event(
                event.name,
                event.startDate,
                event.data.toMutableMap().apply { put(SemanticKeys.COLLECTORNAME, collectorName) }
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
    val userAndPassword = Configuration.getProperty("boudicca.ingest.auth")
        ?: throw IllegalStateException("you need to specify the boudicca.ingest.auth property!")
    val user = userAndPassword.split(":")[0]
    val password = userAndPassword.split(":")[1]
    val eventDb = EventDB(eventDbUrl, user, password)
    return Consumer {
        eventDb.ingestEvents(listOf(it))
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

private fun mapToEnricherEvent(event: Event): events.boudicca.enricher.openapi.model.Event {
    return events.boudicca.enricher.openapi.model.Event()
        .name(event.name)
        .startDate(event.startDate)
        .data(event.data)
}

private fun mapToEventCollectorEvent(event: events.boudicca.enricher.openapi.model.Event): Event {
    return Event(event.name, event.startDate, event.data ?: emptyMap())
}

fun <T> retry(log: Logger, function: () -> T): T {
    var lastException: Throwable? = null
    for (i in 1..5) {
        try {
            return function()
        } catch (e: Exception) {
            lastException = e
            log.info("exception caught, retrying in 1 minute", e)
            Thread.sleep(1000 * 60)
        }
    }
    throw lastException!!
}