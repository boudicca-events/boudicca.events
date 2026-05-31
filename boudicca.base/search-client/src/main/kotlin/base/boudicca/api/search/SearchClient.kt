package base.boudicca.api.search

interface SearchClient {
    fun queryEvents(queryDTO: QueryDTO): SearchResultDTO

    fun queryEntries(queryDTO: QueryDTO): ResultDTO

    fun getFiltersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO
}
