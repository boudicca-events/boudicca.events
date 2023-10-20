package base.boudicca.search.model

data class Filters(
    val categories: Set<String> = setOf(),
    val locationNames: Set<String> = setOf(),
    val locationCities: Set<String> = setOf(),
)