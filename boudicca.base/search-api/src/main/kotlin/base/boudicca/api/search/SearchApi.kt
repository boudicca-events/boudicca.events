package base.boudicca.api.search

import base.boudicca.search.model.*
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface SearchApi {
    @PostMapping(
        "search",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("it is recommended to use the query endpoint", ReplaceWith("/query"), DeprecationLevel.WARNING)
    fun search(@RequestBody searchDTO: SearchDTO): SearchResultDTO

    @GetMapping("filters")
    @Deprecated("use /filtersFor endpoint")
    fun filters(): Filters

    @PostMapping(
        "filtersFor",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun filtersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO

    @PostMapping(
        "query",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("it is recommended to use the query endpoint", ReplaceWith("/queryEntries"), DeprecationLevel.WARNING)
    fun query(@RequestBody queryDTO: QueryDTO): SearchResultDTO

    @PostMapping(
        "queryEntries",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun queryEntries(queryDTO: QueryDTO): ResultDTO
}