package base.boudicca.api.enricher

import base.boudicca.api.enricher.model.EnrichRequestDTO
import base.boudicca.model.Event
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces

@OpenAPIDefinition
interface EnricherApi {
    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "enriches a list of events",
                useReturnTypeSchema = true
            )
        ],
        tags = ["enricher"]
    )
    @POST
    @Path("enrich")
    @Produces("application/json")
    @Consumes("application/json")
    fun enrich(enrichRequestDTO: EnrichRequestDTO): List<Event>

    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "updates internal caches",
                useReturnTypeSchema = true
            )
        ],
        tags = ["enricher"]
    )
    @POST
    @Path("forceUpdate")
    fun forceUpdate()
}
