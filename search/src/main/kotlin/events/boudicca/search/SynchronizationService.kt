package events.boudicca.search

import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventPublisherResourceApi
import events.boudicca.search.model.Event
import io.quarkus.scheduler.Scheduled
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class SynchronizationService @Inject constructor(
    private val synchEvent: javax.enterprise.event.Event<EventsUpdatedEvent>
) {

    private val publisherApi: EventPublisherResourceApi = createEventPublisherApi()

    @Scheduled(every = "1h")
    fun update() {
        updateEvents()
    }

    private fun updateEvents() {
        val events = publisherApi.eventsGet().map { toSearchEvent(it) }.toSet()
        synchEvent.fire(EventsUpdatedEvent(events))
    }

    private fun toSearchEvent(event: events.boudicca.openapi.model.Event): Event {
        return Event(event.name, event.startDate.toZonedDateTime(), event.data)
    }

    private fun createEventPublisherApi(): EventPublisherResourceApi {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(autoDetectUrl())
        return EventPublisherResourceApi(apiClient)
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

data class EventsUpdatedEvent(val events: Set<Event>)