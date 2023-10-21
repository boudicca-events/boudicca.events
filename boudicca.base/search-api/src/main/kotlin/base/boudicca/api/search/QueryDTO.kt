package base.boudicca.api.search

data class QueryDTO(
    val query: String,
    val offset: Int,
    val size: Int = 30,
)