package events.boudicca

import events.boudicca.model.Event
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Path

@ApplicationScoped
@Path("/ingest")
class EventIngestionResource {

    @Inject
    private lateinit var eventService: EventService

    @POST
    @Path("/add")
    fun add(event: Event) {
        eventService.add(event)
    }

}