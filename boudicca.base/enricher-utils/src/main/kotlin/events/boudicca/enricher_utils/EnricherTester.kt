package events.boudicca.enricher_utils

import events.boudicca.SemanticKeys
import events.boudicca.enricher.openapi.api.EnricherControllerApi
import events.boudicca.enricher.openapi.model.EnrichRequestDTO
import events.boudicca.enricher.openapi.model.Event
import events.boudicca.openapi.api.EventPublisherResourceApi

private const val EVENTDB_URL = "http://localhost:8081"
private const val ENRICHER_URL = "http://localhost:8085"

fun main() {

    var startTime = System.currentTimeMillis()
    val events = getEvents()
    println("fetch all events took ${System.currentTimeMillis() - startTime}ms")

    val filteredEvents = events.filter { it.data?.get(SemanticKeys.COLLECTORNAME) == "linz termine" }

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
        printValues("startDate", event.startDate.toString(), enrichedEvent.startDate.toString())
    }
    val oldValues = event.data ?: emptyMap()
    val newValues = enrichedEvent.data?.toMutableMap() ?: mutableMapOf()
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
    val apiClient = events.boudicca.enricher.openapi.ApiClient()
    apiClient.updateBaseUri(ENRICHER_URL)
    val enricherApi = EnricherControllerApi(apiClient)

    return enricherApi.enrich(EnrichRequestDTO().events(originalEvents))
}


fun getEvents(): List<Event> {
    val apiClient = events.boudicca.openapi.ApiClient()
    apiClient.updateBaseUri(EVENTDB_URL)
    val eventdbResource = EventPublisherResourceApi(apiClient)

    return eventdbResource.eventsGet().map { Event().name(it.name).startDate(it.startDate).data(it.data) }
}
