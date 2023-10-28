package api

import base.boudicca.search.model.QueryDTO
import base.boudicca.search.model.ResultDTO
import base.boudicca.search.model.SearchDTO
import base.boudicca.search.model.SearchResultDTO
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
    fun filters(): Any

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