package base.boudicca.api.search

import base.boudicca.Event
import base.boudicca.search.openapi.ApiClient
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

    fun searchQuery(queryDTO: QueryDTO): SearchResultDTO {
        return mapSearchResultDto(searchApi.queryPost(mapQueryDto(queryDTO)))
    }

    fun getFilters(): FiltersDTO {
        return mapToFiltersDTO(searchApi.filtersGet())
    }

    private fun mapToFiltersDTO(filtersGet: Filters): FiltersDTO {
        return FiltersDTO(filtersGet.categories, filtersGet.locationNames, filtersGet.locationCities)
    }

    private fun mapSearchResultDto(searchResultDTO: base.boudicca.search.openapi.model.SearchResultDTO): SearchResultDTO {
        return SearchResultDTO(searchResultDTO.result.map { toEvent(it) }, searchResultDTO.totalResults ?: -1)
    }

    private fun mapQueryDto(queryDTO: QueryDTO): base.boudicca.search.openapi.model.QueryDTO {
        return base.boudicca.search.openapi.model.QueryDTO()
            .query(queryDTO.query)
            .offset(queryDTO.offset)
            .size(queryDTO.size)
    }

    private fun toEvent(enricherEvent: base.boudicca.search.openapi.model.Event): Event {
        return Event(enricherEvent.name, enricherEvent.startDate, enricherEvent.data ?: mapOf())
    }

    private fun mapToEnricherEvent(event: Event): base.boudicca.search.openapi.model.Event {
        return base.boudicca.search.openapi.model.Event()
            .name(event.name)
            .startDate(event.startDate)
            .data(event.data)
    }
}