package at.cnoize.boudicca

import at.cnoize.boudicca.publisherapi.PublisherApi
import at.cnoize.boudicca.model.ComplexSearchDto
import at.cnoize.boudicca.model.Event
import at.cnoize.boudicca.model.SearchDTO
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/events")
class EventPublisherResource : PublisherApi {

    private val eventService = EventService()

    override fun list(): Set<Event> {
        return eventService.list()
    }

    override fun search(searchDTO: SearchDTO): Set<Event> {
        return eventService.search(searchDTO)
    }

    override fun searchBy(complexSearchDto: ComplexSearchDto): Set<Event> {
        return eventService.searchBy(complexSearchDto)
    }

}