package base.boudicca.publisher.event.html.service

import base.boudicca.api.search.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SearchService @Autowired constructor(
  @Value("\${boudicca.search.url}") private val searchUrl: String,
): SearchServiceCaller {
  private val client: SearchClient = createSearchClient(searchUrl)

  override fun search(queryDTO: QueryDTO): SearchResultDTO {
    return client.queryEvents(queryDTO)
  }

  override fun getFiltersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO {
    return client.getFiltersFor(filterQueryDTO)
  }

  fun createSearchClient(searchUrl: String): SearchClient {
    return SearchClient(searchUrl)
  }
}
