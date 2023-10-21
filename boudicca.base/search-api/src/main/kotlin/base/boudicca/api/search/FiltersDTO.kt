package base.boudicca.api.search


data class FiltersDTO(
    val categories: Set<String> = setOf(),
    val locationNames: Set<String> = setOf(),
    val locationCities: Set<String> = setOf(),
)
