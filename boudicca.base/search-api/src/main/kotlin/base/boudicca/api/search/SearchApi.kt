package base.boudicca.api.search

import base.boudicca.api.search.model.*
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces

@OpenAPIDefinition
interface SearchApi {

    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "search with an prefefined dto, is deprecated",
                useReturnTypeSchema = true
            )
        ],
        tags = ["search"]
    )
    @POST
    @Path("search")
    @Produces("application/json")
    @Consumes("application/json")
    @Deprecated("it is recommended to use the query endpoint", ReplaceWith("/query"), DeprecationLevel.WARNING)
    fun search(searchDTO: SearchDTO): SearchResultDTO

    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "get hardcoded filter values, is deprecated",
                useReturnTypeSchema = true
            )
        ],
        tags = ["search"]
    )
    @GET
    @Path("filters")
    @Produces("application/json")
    @Deprecated("use /filtersFor endpoint")
    fun filters(): Filters


    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "get filtervalues for the specified fields to f.E. use in an selectbox",
                useReturnTypeSchema = true
            )
        ],
        tags = ["search"]
    )
    @POST
    @Path("filtersFor")
    @Produces("application/json")
    @Consumes("application/json")
    fun filtersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO

    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "query for events, is deprecated",
                useReturnTypeSchema = true
            )
        ],
        tags = ["search"]
    )
    @POST
    @Path("query")
    @Produces("application/json")
    @Consumes("application/json")
    @Deprecated("it is recommended to use the query endpoint", ReplaceWith("/queryEntries"), DeprecationLevel.WARNING)
    fun query(queryDTO: QueryDTO): SearchResultDTO

    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "query for entries using the Boudicca Query Language",
                useReturnTypeSchema = true
            )
        ],
        tags = ["search"]
    )
    @POST
    @Path("queryEntries")
    @Produces("application/json")
    @Consumes("application/json")
    fun queryEntries(queryDTO: QueryDTO): ResultDTO
}