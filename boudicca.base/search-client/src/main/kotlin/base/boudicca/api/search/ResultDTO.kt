package base.boudicca.api.search

import base.boudicca.Entry

data class ResultDTO(
    val result: List<Entry>,
    val totalResults: Int
)
