package base.boudicca.api.search

import base.boudicca.model.Event
import base.boudicca.openapi.ApiClient
import base.boudicca.search.openapi.api.SearchApi
import base.boudicca.search.openapi.model.FilterQueryEntryDTO
import kotlin.jvm.optionals.getOrNull
import base.boudicca.search.openapi.model.FilterQueryDTO as SearchOpenapiFilterQueryDTO

class SearchClient(private val searchUrl: String) {

    private val searchApi: SearchApi

    init {
        if (searchUrl.isBlank()) {
            throw IllegalStateException("you need to pass an searchUrl!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(searchUrl)

        searchApi = SearchApi(apiClient)
    }

    fun queryEvents(queryDTO: QueryDTO): SearchResultDTO {
        val entries = queryEntries(queryDTO)
        return SearchResultDTO(entries.result.mapNotNull { Event.fromEntry(it).getOrNull() }, entries.totalResults, entries.error)
    }

    fun queryEntries(queryDTO: QueryDTO): ResultDTO {
        try {
            return mapResultDto(searchApi.queryEntries(mapQueryDto(queryDTO)))
        } catch (e: Exception) {
            throw SearchException("could not reach search: $searchUrl", e)
        }
    }

    fun getFiltersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO {
        try {
            return searchApi.filtersFor(mapFilterQueryDTOToApi(filterQueryDTO))
        } catch (e: Exception) {
            throw SearchException("could not reach search: $searchUrl", e)
        }
    }

    private fun mapFilterQueryDTOToApi(filterQueryDTO: FilterQueryDTO): SearchOpenapiFilterQueryDTO {
        return SearchOpenapiFilterQueryDTO()
            .entries(filterQueryDTO.entries.map { FilterQueryEntryDTO().name(it.name)})
    }

    private fun mapResultDto(resultDTO: base.boudicca.search.openapi.model.ResultDTO): ResultDTO {
        return ResultDTO(resultDTO.result!!, resultDTO.totalResults!!, resultDTO.error)
    }

    private fun mapQueryDto(queryDTO: QueryDTO): base.boudicca.search.openapi.model.QueryDTO {
        return base.boudicca.search.openapi.model.QueryDTO()
            .query(queryDTO.query)
            .offset(queryDTO.offset)
            .size(queryDTO.size)
    }
}

class SearchException(msg: String, t: Throwable) : RuntimeException(msg, t)
