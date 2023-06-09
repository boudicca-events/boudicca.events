package events.boudicca.search

import events.boudicca.search.model.Event
import events.boudicca.search.model.Filters
import events.boudicca.search.model.QueryDTO
import events.boudicca.search.model.SearchDTO
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@ApplicationScoped
@Path("/")
class SearchResource @Inject constructor(
    private val searchService: SearchService,
    private val queryService: QueryService,
    private val synchronizationService: SynchronizationService,
) {

    private val localMode = autoDetectLocalMode()

    @Path("search")
    @POST
    fun search(searchDTO: SearchDTO): List<Event> {
        if (localMode) {
            synchronizationService.update()
        }
        return searchService.search(searchDTO)
    }

    @Path("filters")
    @GET
    fun filters(): Filters {
        if (localMode) {
            synchronizationService.update()
        }
        return searchService.filters()
    }

    @Path("query")
    @POST
    fun query(queryDTO: QueryDTO): List<Event> {
        if (localMode) {
            synchronizationService.update()
        }
        return queryService.query(queryDTO)
    }

    private fun autoDetectLocalMode(): Boolean {
        var localMode = System.getenv("BOUDICCA_LOCAL")
        if (localMode != null && localMode.isNotBlank()) {
            return "true" == localMode
        }
        localMode = System.getProperty("boudiccaLocal")
        if (localMode != null && localMode.isNotBlank()) {
            return "true" == localMode
        }
        return false
    }
}
