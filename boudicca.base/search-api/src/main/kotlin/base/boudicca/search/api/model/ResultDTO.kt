package base.boudicca.search.model

import base.boudicca.model.Entry

data class ResultDTO(
    val result: List<Entry>,
    val totalResults: Int
)
