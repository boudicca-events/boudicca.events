package base.boudicca.search.model

import base.boudicca.Event

data class SearchResultDTO(
    val result: List<Event>,
    val totalResults: Int
)
