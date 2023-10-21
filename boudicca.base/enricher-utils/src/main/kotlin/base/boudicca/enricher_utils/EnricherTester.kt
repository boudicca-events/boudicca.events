package base.boudicca.enricher_utils

import base.boudicca.Event
import base.boudicca.SemanticKeys
import base.boudicca.api.enricher.Enricher
import base.boudicca.api.eventdb.publisher.EventDB

private const val EVENTDB_URL = "http://localhost:8081"
private const val ENRICHER_URL = "http://localhost:8085"

fun main() {

    var startTime = System.currentTimeMillis()
    val events = getEvents()
    println("fetch all events took ${System.currentTimeMillis() - startTime}ms")

    val filteredEvents = events.filter { it.data[SemanticKeys.COLLECTORNAME] == "linz termine" }

    startTime = System.currentTimeMillis()
    val enrichedEvents = enrich(filteredEvents)
    println("enrich filtered events took ${System.currentTimeMillis() - startTime}ms")

    compare(filteredEvents, enrichedEvents)
}

fun compare(events: List<Event>, enrichedEvents: List<Event>) {
    if (events.size != enrichedEvents.size) {
        throw IllegalArgumentException("sizes do not match, wat")
    }

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
            enrichedEvent.startDate.toString()
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
    println(String.format("%10s: %10s -> %10s", key, oldValue, newValue))
}

private fun enrich(originalEvents: List<Event>): List<Event> {
    return Enricher(ENRICHER_URL).enrichEvents(originalEvents)
}

fun getEvents(): List<Event> {
    return EventDB(EVENTDB_URL).getAllEvents().toList()
}
