package base.boudicca.api.search.model

import base.boudicca.model.Entry

data class ResultDTO(
    val result: List<Entry>,
    val totalResults: Int,
    val error: String? = null
)
