package events.boudicca.api.eventcollector

import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventIngestionResourceApi
import java.time.Duration

class EventCollectorScheduler {
    private var interval: Duration = Duration.ofHours(24)
    private var boudiccaUrl: String
    private val eventCollectors: MutableList<EventCollector> = mutableListOf()

    init {
        boudiccaUrl = autoDetectUrl()
    }

    fun addEventCollector(eventCollector: EventCollector): EventCollectorScheduler {
        eventCollectors.add(eventCollector)
        return this
    }

    fun setBoudiccaUrl(boudiccaUrl: String): EventCollectorScheduler {
        this.boudiccaUrl = boudiccaUrl
        return this
    }

    fun setInterval(interval: Duration): EventCollectorScheduler {
        this.interval = interval
        return this
    }

    fun run(): Nothing {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(boudiccaUrl)
        val ingestionApi = EventIngestionResourceApi(apiClient)
        while (true) {
            for (eventCollector in eventCollectors) {
                try {
                    collect(eventCollector, ingestionApi)
                } catch (e: Exception) {
                    println("event collector ${eventCollector.getName()} threw execption while collecting")
                    e.printStackTrace()
                }
            }

            println("all event collectors ran, sleeping for $interval")
            Thread.sleep(interval.toMillis())
        }
    }

    private fun collect(eventCollector: EventCollector, ingestionApi: EventIngestionResourceApi) {
        val events = eventCollector.collectEvents()
        for (event in events) {
            ingestionApi.ingestAddPost(event)
        }
        println("collected ${events.size} events for event collector ${eventCollector.getName()}")
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
}