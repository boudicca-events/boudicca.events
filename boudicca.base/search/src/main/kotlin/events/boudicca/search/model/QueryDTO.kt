package events.boudicca.search.model

data class QueryDTO(
    val query: String? = null,
    val offset: Int? = null,
    val size: Int? = null,
)