package base.boudicca.api.eventcollector

import base.boudicca.api.enricher.EnricherClient
import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.model.Event
import java.time.Duration
import java.util.function.Consumer
import java.util.function.Function

class EventCollectorCoordinatorBuilder {

    private val eventCollectors: MutableList<EventCollector> = mutableListOf()
    private var interval: Duration = Duration.ofHours(24)
    private var eventSink = createBoudiccaEventSink(Configuration.getProperty("boudicca.eventdb.url"))
    private var enricherFunction = createBoudiccaEnricherFunction(Configuration.getProperty("boudicca.enricher.url"))

    fun addEventCollector(eventCollector: EventCollector): EventCollectorCoordinatorBuilder {
        eventCollectors.add(eventCollector)
        return this
    }

    fun addEventCollectors(eventCollectors: Collection<EventCollector>): EventCollectorCoordinatorBuilder {
        this.eventCollectors.addAll(eventCollectors)
        return this
    }

    fun setCollectionInterval(interval: Duration): EventCollectorCoordinatorBuilder {
        this.interval = interval
        return this
    }

    fun setEventSink(eventSink: Consumer<List<Event>>): EventCollectorCoordinatorBuilder {
        this.eventSink = eventSink
        return this
    }

    fun setEnricherFunction(enricherFunction: Function<List<Event>, List<Event>>): EventCollectorCoordinatorBuilder {
        this.enricherFunction = enricherFunction
        return this
    }

    fun build(): EventCollectorCoordinator {
        val finalEventCollectors = eventCollectors.toList()
        return EventCollectorCoordinator(
            interval,
            finalEventCollectors, // make a copy to make it basically immutable
            EventCollectionRunner(finalEventCollectors, eventSink, enricherFunction)
        )
    }

    private fun createBoudiccaEventSink(eventDbUrl: String?): Consumer<List<Event>> {
        if (eventDbUrl.isNullOrBlank()) {
            throw IllegalStateException("you need to specify the boudicca.eventdb.url property!")
        }
        val userAndPassword = Configuration.getProperty("boudicca.ingest.auth")
            ?: throw IllegalStateException("you need to specify the boudicca.ingest.auth property!")
        val user = userAndPassword.split(":")[0]
        val password = userAndPassword.split(":")[1]
        val eventDb = EventDbIngestClient(eventDbUrl, user, password)
        return Consumer {
            eventDb.ingestEvents(it)
        }
    }

    private fun createBoudiccaEnricherFunction(enricherUrl: String?): Function<List<Event>, List<Event>>? {
        if (enricherUrl.isNullOrBlank()) {
            return null
        }
        val enricher = EnricherClient(enricherUrl)
        return Function<List<Event>, List<Event>> { events ->
            enricher.enrichEvents(events)
        }
    }
}
