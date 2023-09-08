package events.boudicca.eventdb

import events.boudicca.eventdb.model.ComplexSearchDto
import events.boudicca.eventdb.model.Event
import events.boudicca.eventdb.model.SearchDTO
import javax.annotation.security.PermitAll
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@PermitAll
@ApplicationScoped
@Path("/events")
class EventPublisherResource @Inject constructor(
    private val eventService: EventService,
    private val eventSearchService: EventSearchService,
){

    @GET
    fun list(): Set<Event> {
        return eventService.list()
    }

    @Path("search")
    @POST
    fun search(searchDTO: SearchDTO): Set<Event> {
        return eventSearchService.search(searchDTO)
    }

    @Path("searchBy")
    @POST
    fun searchBy(complexSearchDto: ComplexSearchDto): Set<Event> {
        return eventSearchService.searchBy(complexSearchDto)
    }

}