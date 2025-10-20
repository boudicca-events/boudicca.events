package base.boudicca.api.eventdb

import base.boudicca.model.Entry
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.util.UUID
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces

@OpenAPIDefinition
interface PublisherApi {
    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "returns all entries from the event db",
                useReturnTypeSchema = true
            )
        ],
        tags = ["publisher"]
    )
    @GET
    @Path("entries")
    @Produces("application/json")
    fun all(): Set<Entry>

    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "returns a single entry by id from the db",
                useReturnTypeSchema = true
            ),
            ApiResponse(
                responseCode = "404",
                description = "entry not found"
            )
        ],
        tags = ["publisher"]
    )
    @GET
    @Path("entry/{boudiccaId}")
    @Produces("application/json")
    fun entry(@PathParam("boudiccaId") boudiccaId: UUID): Entry?
}
