package base.boudicca.api.eventcollector

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.runner.RunnerEnricherInterface
import base.boudicca.api.eventcollector.runner.RunnerIngestionInterface
import base.boudicca.fetcher.retry
import base.boudicca.model.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

class EventCollectionRunner(
    private val eventCollectors: List<EventCollector>,
    private val ingestionInterface: RunnerIngestionInterface,
    private val enricherInterface: RunnerEnricherInterface,
    otel: OpenTelemetry = OpenTelemetry.noop(),
) {
    private val logger = KotlinLogging.logger {}
    private val executor = Executors.newVirtualThreadPerTaskExecutor()
    private val tracer = otel.getTracer("EventCollectionRunner")

    /**
     * executes a full collection for all configured eventcollectors
     */
    fun run() {
        val span = tracer.spanBuilder("full collection").setSpanKind(SpanKind.INTERNAL).startSpan()
        try {
            span.makeCurrent().use {
                logger.info { "starting new full collection" }
                Collections.startFullCollection()
                try {
                    val totalEventsCollected = AtomicLong()
                    eventCollectors.map {
                        executor.submit {
                            val eventsCollected = collect(it, span)
                            totalEventsCollected.updateAndGet { it + eventsCollected }
                        }
                    }.forEach { it.get() }
                    span.setAttribute("collected_events", totalEventsCollected.get())
                } finally {
                    Collections.endFullCollection()
                }
                logger.info { "full collection done" }
            }
        } finally {
            span.end()
        }
    }

    private fun collect(eventCollector: EventCollector, parentSpan: Span): Long {
        val span =
            tracer.spanBuilder("single collection")
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("collector", eventCollector.getName())
                .setParent(Context.current().with(parentSpan))
                .startSpan()

        return span.makeCurrent().use {
            Collections.startSingleCollection(eventCollector)
            try {
                val events = eventCollector.collectEvents()
                Collections.getCurrentSingleCollection()!!.totalEventsCollected = events.size
                validateCollection(eventCollector, events)
                try {
                    val postProcessedEvents = events.map { postProcess(it, eventCollector.getName()) }
                    val enrichedEvents = enrich(postProcessedEvents)
                    retry(logger) {
                        ingestionInterface.ingestEvents(enrichedEvents)
                    }
                    span.setAttribute("collected_events", events.size.toLong())
                    return@use events.size.toLong()
                } catch (e: RuntimeException) {
                    logger.error(e) { "could not ingest events, is the eventdb/enricher available?" }
                    return@use 0
                }
            } catch (e: Exception) {
                logger.error(e) { "collector threw exception while collecting" }
                return@use 0
            } finally {
                Collections.endSingleCollection()
                span.end()
            }
        }
    }

    private fun validateCollection(eventCollector: EventCollector, events: List<Event>) {
        if (events.isEmpty()) {
            logger.warn { "collector collected 0 events!" }
        }
        val nonBlankFields = mutableSetOf<String>()
        val allFields = mutableSetOf<String>()
        for (event in events) {
            if (event.name.isBlank()) {
                logger.warn { "event has empty name: $event" }
            }
            if (event.toStructuredEvent().getProperty(SemanticKeys.SOURCES_PROPERTY).isEmpty()) {
                logger.error { "event has no sources: $event" }
            }
            for (entry in event.data.entries) {
                allFields.add(entry.key)
                if (entry.value.isNotBlank()) {
                    nonBlankFields.add(entry.key)
                }
            }
        }
        for (field in allFields.minus(nonBlankFields)) {
            logger.warn {
                "eventcollector ${eventCollector.getName()} " + "has blank values for all events for field $field"
            }
        }
    }

    private fun enrich(events: List<Event>): List<Event> {
        return try {
            retry(logger) {
                enricherInterface.enrichEvents(events)
            }
        } catch (e: Exception) {
            logger.error(e) { "enricher threw exception, submitting events un-enriched" }
            events
        }
    }

    private fun postProcess(event: Event, collectorName: String): Event {
        if (!event.data.containsKey(SemanticKeys.COLLECTORNAME)) {
            return Event(
                event.name,
                event.startDate,
                event.data.toMutableMap().apply { put(SemanticKeys.COLLECTORNAME, collectorName) },
            )
        }
        return event
    }
}
