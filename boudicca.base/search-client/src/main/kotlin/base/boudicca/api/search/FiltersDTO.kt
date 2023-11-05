package base.boudicca.api.search


@Deprecated("use FilterQueryDTO/FilterResultDTO")
data class FiltersDTO(
    val locationNames: Set<String> = setOf(),
    val locationCities: Set<String> = setOf(),
)
