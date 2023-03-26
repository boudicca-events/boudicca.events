package at.cnoize.boudicca

import java.util.*
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/event")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EventResource {


    private val eventService = EventService()

    @GET
    fun list(): Set<Event> {
        return eventService.list()
    }

    @POST
    fun add(event: Event) {
        eventService.add(event)
    }

    @Path("search")
    @POST
    fun search(searchDTO: SearchDTO): Set<Event> {
        return eventService.search(searchDTO)
    }

    @Path("searchBy")
    @POST
    fun searchBy(complexSearchDto: ComplexSearchDto    ): Set<Event> {
        return eventService.searchBy(complexSearchDto)
    }
}
