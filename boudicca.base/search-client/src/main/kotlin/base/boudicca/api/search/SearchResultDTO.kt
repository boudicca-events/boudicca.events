package base.boudicca.api.search

import base.boudicca.Event

data class SearchResultDTO(
    val result: List<Event>,
    val totalResults: Int
)
