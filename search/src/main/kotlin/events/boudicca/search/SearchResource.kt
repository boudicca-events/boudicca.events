package events.boudicca.search

import events.boudicca.search.model.Event
import events.boudicca.search.model.Filters
import events.boudicca.search.model.SearchDTO
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@ApplicationScoped
@Path("/")
class SearchResource @Inject constructor(private var searchService: SearchService) {

    @Path("search")
    @POST
    fun search(searchDTO: SearchDTO): List<Event> {
        return searchService.search(searchDTO)
    }

    @Path("filters")
    @GET
    fun filters(): Filters {
        return searchService.filters()
    }

}