package base.boudicca.api.search.model

data class FilterQueryDTO(val entries: List<FilterQueryEntryDTO>)

data class FilterQueryEntryDTO(val name: String)
