package events.boudicca.eventdb

import events.boudicca.eventdb.model.Event
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Path

@RolesAllowed("ingest")
@ApplicationScoped
@Path("/ingest")
class EventIngestionResource @Inject constructor(private val eventService: EventService) {

    @POST
    @Path("/add")
    fun add(event: Event) {
        eventService.add(event)
    }

}