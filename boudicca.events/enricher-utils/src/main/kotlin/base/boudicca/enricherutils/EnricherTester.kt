package base.boudicca.enricherutils

import base.boudicca.api.enricher.EnricherClient
import base.boudicca.api.eventdb.publisher.EventDbPublisherClient
import base.boudicca.model.Event
import java.util.*

private const val EVENTDB_URL = "http://localhost:8081"
private const val ENRICHER_URL = "http://localhost:8085"

fun main() {
    var startTime = System.currentTimeMillis()
    val events = getEvents()
    println("fetch all events took ${System.currentTimeMillis() - startTime}ms")

    val filteredEvents = events // .filter { it.data[SemanticKeys.COLLECTORNAME] == "posthof" }

    startTime = System.currentTimeMillis()
    val enrichedEvents = enrich(filteredEvents)
    println("enrich filtered events took ${System.currentTimeMillis() - startTime}ms")

    compare(filteredEvents, enrichedEvents)
}

fun compare(events: List<Event>, enrichedEvents: List<Event>) {
    require(events.size == enrichedEvents.size) { "sizes do not match, wat" }

    for (i in events.indices) {
        if (events[i] != enrichedEvents[i]) {
            printDiff(events[i], enrichedEvents[i])
        }
    }
}

fun printDiff(event: Event, enrichedEvent: Event) {
    println()
    printValues("name", event.name, enrichedEvent.name)
    if (event.startDate != enrichedEvent.startDate) {
        printValues(
            "startDate",
            event.startDate.toString(),
            enrichedEvent.startDate.toString(),
        )
    }
    val oldValues = event.data
    val newValues = enrichedEvent.data.toMutableMap()
    for (key in oldValues.keys.sorted()) {
        if (oldValues[key] != newValues[key]) {
            printValues(key, oldValues[key], newValues[key])
        }
        newValues.remove(key)
    }
    for (key in newValues.keys.sorted()) {
        printValues(key, null, newValues[key])
    }
}

fun printValues(key: String, oldValue: String?, newValue: String?) {
    println(String.format(Locale.getDefault(), "%10s: %10s -> %10s", key, oldValue, newValue))
}

private fun enrich(originalEvents: List<Event>): List<Event> = EnricherClient(ENRICHER_URL).enrichEvents(originalEvents)

fun getEvents(): List<Event> = EventDbPublisherClient(EVENTDB_URL).getAllEvents().toList()
