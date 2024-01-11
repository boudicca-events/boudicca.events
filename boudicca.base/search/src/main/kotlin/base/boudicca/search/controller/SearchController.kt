package base.boudicca.search.controller

import base.boudicca.api.search.SearchApi
import base.boudicca.api.search.model.*
import base.boudicca.model.Event
import base.boudicca.search.BoudiccaSearchProperties
import base.boudicca.search.service.QueryService
import base.boudicca.search.service.SearchService
import base.boudicca.search.service.SynchronizationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class SearchController @Autowired constructor(
    private val searchService: SearchService,
    private val queryService: QueryService,
    private val synchronizationService: SynchronizationService,
    private val boudiccaSearchProperties: BoudiccaSearchProperties
) : SearchApi {

    @Deprecated("it is recommended to use the query endpoint", ReplaceWith("/query"), DeprecationLevel.WARNING)
    override fun search(@RequestBody searchDTO: SearchDTO): SearchResultDTO {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        val result = searchService.search(searchDTO)
        return SearchResultDTO(result.result.mapNotNull { Event.fromEntry(it) }, result.totalResults)
    }

    override fun filters(): Filters {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        return searchService.filters()
    }

    override fun filtersFor(@RequestBody filterQueryDTO: FilterQueryDTO): FilterResultDTO {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        return searchService.filtersFor(filterQueryDTO)
    }

    @Deprecated("it is recommended to use the query endpoint", ReplaceWith("/queryEntries"), DeprecationLevel.WARNING)
    override fun query(@RequestBody queryDTO: QueryDTO): SearchResultDTO {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        val result = queryService.query(queryDTO)
        return SearchResultDTO(result.result.mapNotNull { Event.fromEntry(it) }, result.totalResults)
    }

    override fun queryEntries(@RequestBody queryDTO: QueryDTO): ResultDTO {
        if (boudiccaSearchProperties.localMode) {
            synchronizationService.update()
        }
        return queryService.query(queryDTO)
    }
}
