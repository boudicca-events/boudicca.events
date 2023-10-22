package base.boudicca.search.model

import base.boudicca.Event

@Deprecated("use ResultDTO")
data class SearchResultDTO(
    val result: List<Event>,
    val totalResults: Int
)
