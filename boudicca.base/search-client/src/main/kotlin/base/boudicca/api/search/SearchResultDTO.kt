package base.boudicca.api.search

import base.boudicca.model.Event

data class SearchResultDTO(
    val result: List<Event>,
    val totalResults: Int,
    val error: String?,
)
