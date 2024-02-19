package base.boudicca.api.search

import base.boudicca.model.Entry

data class ResultDTO(
    val result: List<Entry>,
    val totalResults: Int,
    val error: String?
)
