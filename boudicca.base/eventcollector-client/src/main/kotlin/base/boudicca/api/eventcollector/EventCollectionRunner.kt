package base.boudicca.api.eventcollector

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.util.retry
import base.boudicca.model.Event
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.Executors
import java.util.function.Consumer
import java.util.function.Function


class EventCollectionRunner(
    private val eventCollectors: List<EventCollector>,
    private val eventSink: Consumer<List<Event>>,
    private val enricherFunction: Function<List<Event>, List<Event>>?,
) {

    private val LOG = LoggerFactory.getLogger(this::class.java)
    private val executor = Executors.newCachedThreadPool { Thread(it).apply { isDaemon = true } } //TODO change to virtualthreads

    /**
     * executes a full collection for all configured eventcollectors
     */
    fun run() {
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
            validateCollection(eventCollector, events)
            try {
                val postProcessedEvents = events.map { postProcess(it, eventCollector.getName()) }
                val enrichedEvents = enrich(postProcessedEvents)
                retry(LOG) {
                    eventSink.accept(enrichedEvents)
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

    private fun validateCollection(eventCollector: EventCollector, events: List<Event>) {
        if (events.isEmpty()) {
            LOG.warn("collector collected 0 events!")
        }
        val nonBlankFields = mutableSetOf<String>()
        val allFields = mutableSetOf<String>()
        for (event in events) {
            if (event.name.isBlank()) {
                LOG.warn("event has empty name: $event")
            }
            if (event.data[SemanticKeys.SOURCES].isNullOrBlank()) {
                LOG.error("event has no sources: $event")
            }
            for (entry in event.data.entries) {
                allFields.add(entry.key)
                if (entry.value.isNotBlank()) {
                    nonBlankFields.add(entry.key)
                }
            }
        }
        for (field in allFields.minus(nonBlankFields)) {
            LOG.warn("eventcollector ${eventCollector.getName()} has blank values for all events for field $field")
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
        if (!event.data.containsKey(SemanticKeys.COLLECTORNAME)) {
            return Event(
                event.name,
                event.startDate,
                event.data.toMutableMap().apply { put(SemanticKeys.COLLECTORNAME, collectorName) }
            )
        }
        return event
    }

}