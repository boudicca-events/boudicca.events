package events.boudicca.search.controller

import events.boudicca.search.service.SynchronizationService
import events.boudicca.search.model.Filters
import events.boudicca.search.model.QueryDTO
import events.boudicca.search.model.SearchDTO
import events.boudicca.search.model.SearchResultDTO
import events.boudicca.search.service.QueryService
import events.boudicca.search.service.SearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/")
@RestController
class SearchResource @Autowired constructor(
    private val searchService: SearchService,
    private val queryService: QueryService,
    private val synchronizationService: SynchronizationService,
    @Value("\${boudicca.local.mode}") private val localMode: Boolean,
) {

    @PostMapping(
        "search",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun search(@RequestBody searchDTO: SearchDTO): SearchResultDTO {
        if (localMode) {
            synchronizationService.update()
        }
        return searchService.search(searchDTO)
    }

    @GetMapping("filters")
    fun filters(): Filters {
        if (localMode) {
            synchronizationService.update()
        }
        return searchService.filters()
    }

    @PostMapping(
        "query",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun query(@RequestBody queryDTO: QueryDTO): SearchResultDTO {
        if (localMode) {
            synchronizationService.update()
        }
        return queryService.query(queryDTO)
    }
}
