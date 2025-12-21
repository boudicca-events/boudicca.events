package base.boudicca.publisher.event.html.service

import base.boudicca.api.search.FilterQueryDTO
import base.boudicca.api.search.FilterResultDTO
import base.boudicca.api.search.QueryDTO
import base.boudicca.api.search.SearchClient
import base.boudicca.api.search.SearchResultDTO
import io.opentelemetry.api.OpenTelemetry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SearchServiceCallerImpl
@Autowired
constructor(
    @Value("\${boudicca.search.url}") private val searchUrl: String,
    otel: OpenTelemetry,
) : SearchServiceCaller {
    private val client: SearchClient = createSearchClient(searchUrl, otel)

    override fun search(queryDTO: QueryDTO): SearchResultDTO {
        return client.queryEvents(queryDTO)
    }

    override fun getFiltersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO {
        return client.getFiltersFor(filterQueryDTO)
    }

    fun createSearchClient(searchUrl: String, otel: OpenTelemetry): SearchClient {
        return SearchClient(searchUrl, otel)
    }
}
