package events.boudicca

import events.boudicca.model.Event
import events.boudicca.model.SearchDTO
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Path

@ApplicationScoped
@Path("/")
class SearchResource {

    @Inject
    private lateinit var searchService: SearchService

    @Path("search")
    @POST
    fun search(searchDTO: SearchDTO): Set<Event> {
        return searchService.search(searchDTO)
    }

}