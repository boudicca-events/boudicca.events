package base.boudicca.publisher.event.html.service

import base.boudicca.SemanticKeys
import base.boudicca.api.search.FilterQueryDTO
import base.boudicca.api.search.FilterQueryEntryDTO
import org.springframework.stereotype.Service
import java.net.URI

@Service
class SourcesService(
    private val caller: SearchServiceCaller,
) {
    fun getSources(): List<String> {
        val allSources =
            caller.getFiltersFor(FilterQueryDTO(listOf(FilterQueryEntryDTO(SemanticKeys.SOURCES))))
        return allSources[SemanticKeys.SOURCES]
            ?.map(::normalize)
            ?.distinct()
            ?.sortedBy { it }
            ?: listOf("no Sources found")
    }

    private fun normalize(value: String): String {
        return if (value.startsWith("http")) {
            //treat as url
            try {
                URI.create(value).normalize().host ?: value
            } catch (_: IllegalArgumentException) {
                //hm, no url?
                value
            }
        } else {
            value
        }
    }
}
