package events.boudicca.api.eventcollector

import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventIngestionResourceApi
import java.time.Duration

class EventCollectorScheduler(
    private val interval: Duration = Duration.ofHours(24),
    boudiccaUrl: String = autoDetectUrl()
) {
    private val eventCollectors: MutableList<EventCollector> = mutableListOf()
    private val ingestionApi: EventIngestionResourceApi

    init {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(boudiccaUrl)
        ingestionApi = EventIngestionResourceApi(apiClient)
    }

    fun addEventCollector(eventCollector: EventCollector): EventCollectorScheduler {
        eventCollectors.add(eventCollector)
        return this
    }

    fun run(): Nothing {
        while (true) {
            runOnce()
            Thread.sleep(interval.toMillis())
        }
    }

    fun runOnce() {
        for (eventCollector in eventCollectors) {
            try {
                collect(eventCollector)
            } catch (e: Exception) {
                println("event collector ${eventCollector.getName()} threw execption while collecting")
                e.printStackTrace()
            }
        }

        println("all event collectors ran, sleeping for $interval")
    }

    private fun collect(eventCollector: EventCollector) {
        val events = eventCollector.collectEvents()
        for (event in events) {
            ingestionApi.ingestAddPost(mapToApiEvent(event))
        }
        println("collected ${events.size} events for event collector ${eventCollector.getName()}")
    }

    private fun mapToApiEvent(event: Event): events.boudicca.openapi.model.Event {
        return events.boudicca.openapi.model.Event()
            .name(event.name)
            .startDate(event.startDate)
            .data(event.additionalData)
    }

}

fun autoDetectUrl(): String {
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