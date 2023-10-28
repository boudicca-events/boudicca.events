package base.boudicca.api.search

import base.boudicca.Event
import base.boudicca.search.openapi.ApiClient
import base.boudicca.search.openapi.ApiException
import base.boudicca.search.openapi.api.SearchResourceApi
import base.boudicca.search.openapi.model.Filters

class Search(enricherUrl: String) {

    private val searchApi: SearchResourceApi

    init {
        if (enricherUrl.isBlank()) {
            throw IllegalStateException("you need to pass an eventDbUrl!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(enricherUrl)

        searchApi = SearchResourceApi(apiClient)
    }

    @Deprecated("use query Events")
    fun searchQuery(queryDTO: QueryDTO): SearchResultDTO {
        return queryEvents(queryDTO)
    }

    fun queryEvents(queryDTO: QueryDTO): SearchResultDTO {
        val entries = queryEntries(queryDTO)
        return SearchResultDTO(entries.result.mapNotNull { Event.fromEntry(it) }, entries.totalResults)
    }

    fun queryEntries(queryDTO: QueryDTO): ResultDTO {
        try {
            return mapResultDto(searchApi.queryEntries(mapQueryDto(queryDTO)))
        } catch (e: ApiException) {
            throw SearchException("could not reach eventdb", e)
        }
    }

    fun getFilters(): FiltersDTO {
        try {
            return mapToFiltersDTO(searchApi.filters())
        } catch (e: ApiException) {
            throw SearchException("could not reach eventdb", e)
        }
    }

    private fun mapToFiltersDTO(filtersGet: Filters): FiltersDTO {
        return FiltersDTO(filtersGet.locationNames, filtersGet.locationCities)
    }

    private fun mapResultDto(resultDTO: base.boudicca.search.openapi.model.ResultDTO): ResultDTO {
        return ResultDTO(resultDTO.result, resultDTO.totalResults)
    }

    private fun mapQueryDto(queryDTO: QueryDTO): base.boudicca.search.openapi.model.QueryDTO {
        return base.boudicca.search.openapi.model.QueryDTO()
            .query(queryDTO.query)
            .offset(queryDTO.offset)
            .size(queryDTO.size)
    }
}

class SearchException(msg: String, e: ApiException) : RuntimeException(msg, e)