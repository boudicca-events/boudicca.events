package base.boudicca.search.controller

import base.boudicca.Event
import base.boudicca.search.BoudiccaSearchProperties
import base.boudicca.search.model.*
import base.boudicca.search.service.QueryService
import base.boudicca.search.service.SearchService
import base.boudicca.search.service.SynchronizationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RequestMapping("/")
@RestController
class SearchResource @Autowired constructor(
    private val searchService: SearchService,
    private val queryService: QueryService,
    private val synchronizationService: SynchronizationService,
    private val boudiccaSearchProperties: BoudiccaSearchProperties
) {

    @Deprecated("it is recommended to use the query endpoint", ReplaceWith("/query"), DeprecationLevel.WARNING)
    @PostMapping(
        "search",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun search(@RequestBody searchDTO: SearchDTO): SearchResultDTO {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        val result = searchService.search(searchDTO)
        return SearchResultDTO(result.result.mapNotNull { Event.fromEntry(it) }, result.totalResults)
    }

    @GetMapping("filters")
    fun filters(): Filters {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        return searchService.filters()
    }

    @PostMapping(
        "query",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("it is recommended to use the query endpoint", ReplaceWith("/queryEntries"), DeprecationLevel.WARNING)
    fun query(@RequestBody queryDTO: QueryDTO): SearchResultDTO {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        val result = queryService.query(queryDTO)
        return SearchResultDTO(result.result.mapNotNull { Event.fromEntry(it) }, result.totalResults)
    }

    @PostMapping(
        "queryEntries",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun queryEntries(@RequestBody queryDTO: QueryDTO): ResultDTO {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        return queryService.query(queryDTO)
    }
}
