package base.boudicca.api.search

import base.boudicca.api.search.model.*
import io.swagger.annotations.Api
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Api("SearchApi")
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
    fun filtersFor(@RequestBody filterQueryDTO: FilterQueryDTO): FilterResultDTO

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
    fun queryEntries(@RequestBody queryDTO: QueryDTO): ResultDTO
}