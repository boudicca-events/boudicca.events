package base.boudicca.search.model

data class SearchResultDTO(
    val result: List<Event>,
    val totalResults: Int
)
