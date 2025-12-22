package base.boudicca.api.search

import base.boudicca.api.search.model.FilterQueryDTO
import base.boudicca.api.search.model.FilterResultDTO
import base.boudicca.api.search.model.QueryDTO
import base.boudicca.api.search.model.ResultDTO
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces

@OpenAPIDefinition
interface SearchApi {
    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "get filtervalues for the specified fields to f.E. use in an selectbox",
                useReturnTypeSchema = true,
            ),
        ],
        tags = ["search"],
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
                description = "query for entries using the Boudicca Query Language",
                useReturnTypeSchema = true,
            ),
        ],
        tags = ["search"],
    )
    @POST
    @Path("queryEntries")
    @Produces("application/json")
    @Consumes("application/json")
    fun queryEntries(queryDTO: QueryDTO): ResultDTO
}
