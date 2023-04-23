package at.cnoize.boudicca

import at.cnoize.boudicca.crawlerapi.IngestionApi
import at.cnoize.boudicca.model.Event
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