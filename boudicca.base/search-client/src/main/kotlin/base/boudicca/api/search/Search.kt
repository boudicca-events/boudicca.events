package base.boudicca.api.search

import base.boudicca.model.Event
import base.boudicca.openapi.ApiClient
import base.boudicca.openapi.ApiException
import base.boudicca.search.openapi.api.SearchControllerApi
import base.boudicca.search.openapi.model.FilterQueryEntryDTO
import base.boudicca.search.openapi.model.Filters
import base.boudicca.search.openapi.model.FilterQueryDTO as SearchOpenapiFilterQueryDTO

class Search(enricherUrl: String) {

    private val searchApi: SearchControllerApi

    init {
        if (enricherUrl.isBlank()) {
            throw IllegalStateException("you need to pass an eventDbUrl!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(enricherUrl)

        searchApi = SearchControllerApi(apiClient)
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

    @Deprecated("use getFiltersFor method")
    fun getFilters(): FiltersDTO {
        try {
            return mapToFiltersDTO(searchApi.filters())
        } catch (e: ApiException) {
            throw SearchException("could not reach eventdb", e)
        }
    }

    fun getFiltersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO {
        try {
            return searchApi.filtersFor(mapFilterQueryDTOToApi(filterQueryDTO))
        } catch (e: ApiException) {
            throw SearchException("could not reach eventdb", e)
        }
    }

    private fun mapFilterQueryDTOToApi(filterQueryDTO: FilterQueryDTO): SearchOpenapiFilterQueryDTO {
        return SearchOpenapiFilterQueryDTO()
            .entries(filterQueryDTO.entries.map { FilterQueryEntryDTO().name(it.name).multiline(it.multiline) })
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