package at.cnoize.boudicca

import at.cnoize.boudicca.publisherapi.PublisherApi
import at.cnoize.boudicca.model.ComplexSearchDto
import at.cnoize.boudicca.model.Event
import at.cnoize.boudicca.model.SearchDTO
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/events")
class EventPublisherResource : PublisherApi {

    @Inject
    private lateinit var eventService: EventService

    @GET
    override fun list(): Set<Event> {
        return eventService.list()
    }

    @Path("search")
    @POST
    override fun search(searchDTO: SearchDTO): Set<Event> {
        return eventService.search(searchDTO)
    }

    @Path("searchBy")
    @POST
    override fun searchBy(complexSearchDto: ComplexSearchDto): Set<Event> {
        return eventService.searchBy(complexSearchDto)
    }

}