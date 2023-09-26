package events.boudicca.search.service

import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventPublisherResourceApi
import events.boudicca.search.model.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class SynchronizationService @Autowired constructor(
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${boudicca.eventdb.url}") private val eventDbUrl: String,
) {

    private val publisherApi: EventPublisherResourceApi = createEventPublisherApi()

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    fun update() {
        updateEvents()
    }

    private fun updateEvents() {
        val events = publisherApi.eventsGet().map { toSearchEvent(it) }.toSet()
        eventPublisher.publishEvent(EventsUpdatedEvent(events))
    }

    private fun toSearchEvent(event: events.boudicca.openapi.model.Event): Event {
        return Event(event.name, event.startDate.toZonedDateTime(), event.data)
    }

    private fun createEventPublisherApi(): EventPublisherResourceApi {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(eventDbUrl)
        return EventPublisherResourceApi(apiClient)
    }
}

data class EventsUpdatedEvent(val events: Set<Event>)