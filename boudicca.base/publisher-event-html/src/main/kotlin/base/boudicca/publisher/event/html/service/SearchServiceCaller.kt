package base.boudicca.publisher.event.html.service

import base.boudicca.api.search.FilterQueryDTO
import base.boudicca.api.search.FilterResultDTO
import base.boudicca.api.search.QueryDTO
import base.boudicca.api.search.SearchResultDTO

interface SearchServiceCaller {
  fun queryEvents(query: QueryDTO): SearchResultDTO
  fun getFiltersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO
}
