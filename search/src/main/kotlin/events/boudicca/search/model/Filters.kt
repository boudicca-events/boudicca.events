package events.boudicca.search.model

data class Filters(
    val types: Set<String> = setOf(),
    val locationNames: Set<String> = setOf(),
    val locationCities: Set<String> = setOf(),
)