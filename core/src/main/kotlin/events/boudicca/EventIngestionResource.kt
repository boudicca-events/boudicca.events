package events.boudicca

import events.boudicca.crawlerapi.IngestionApi
import events.boudicca.model.Event
import javax.inject.Inject
import javax.ws.rs.Path

@Path("/ingest")
class EventIngestionResource : IngestionApi {

    @Inject
    private lateinit var eventService: EventService

    override fun add(event: Event) {
        eventService.add(event)
    }

}