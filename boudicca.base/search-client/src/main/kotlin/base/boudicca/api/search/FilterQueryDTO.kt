package base.boudicca.api.search

data class FilterQueryDTO(
    val entries: List<FilterQueryEntryDTO>
)

data class FilterQueryEntryDTO(
    val name: String,
    val multiline: Boolean = false
)

