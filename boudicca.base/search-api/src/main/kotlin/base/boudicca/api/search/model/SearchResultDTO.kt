package base.boudicca.api.search.model

import base.boudicca.model.Event

@Deprecated("use ResultDTO")
data class SearchResultDTO(
    val result: List<Event>,
    val totalResults: Int
)
