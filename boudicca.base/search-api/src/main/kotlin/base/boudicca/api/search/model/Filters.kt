package base.boudicca.api.search.model

@Deprecated("use FiltersQueryDTO/FiltersResultDTO endpoint")
data class Filters(
    @Deprecated("do use the EventCategory enum")
    val categories: Set<String> = setOf(),
    val locationNames: Set<String> = setOf(),
    val locationCities: Set<String> = setOf(),
)

