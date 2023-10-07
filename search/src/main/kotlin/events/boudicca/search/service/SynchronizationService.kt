package events.boudicca.search.service

import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.ApiException
import events.boudicca.openapi.api.EventPublisherResourceApi
import events.boudicca.search.model.Event
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Service
class SynchronizationService @Autowired constructor(
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${boudicca.eventdb.url}") private val eventDbUrl: String,
) {

    private val LOG = LoggerFactory.getLogger(this.javaClass)

    private val publisherApi: EventPublisherResourceApi = createEventPublisherApi()
    private val updateLock = ReentrantLock()

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    fun update() {
        updateEvents()
    }

    private fun updateEvents() {
        updateLock.lock()
        try {
            try {
                val events = publisherApi.eventsGet().map { toSearchEvent(it) }.toSet()
                eventPublisher.publishEvent(EventsUpdatedEvent(events))
            } catch (e: ApiException) {
                LOG.warn("could not reach eventdb, retrying in 30s", e)
                //if eventdb is currently down, retry in 30 seconds
                //this mainly happens when both are deployed at the same time
                Thread {
                    Thread.sleep(30000)
                    updateEvents()
                }.start()
            }
        } finally {
            updateLock.unlock()
        }
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