package base.boudicca.api.search


data class FiltersDTO(
    val locationNames: Set<String> = setOf(),
    val locationCities: Set<String> = setOf(),
)
