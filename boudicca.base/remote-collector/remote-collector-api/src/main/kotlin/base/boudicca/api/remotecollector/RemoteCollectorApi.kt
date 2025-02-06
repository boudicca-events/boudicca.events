package base.boudicca.api.remotecollector

import base.boudicca.api.remotecollector.model.EventCollection
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

@OpenAPIDefinition
interface RemoteCollectorApi {
    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "trigger a EventCollection",
                useReturnTypeSchema = true
            )
        ],
        tags = ["remote-collector"]
    )
    @GET
    @Path("collectEvents")
    @Produces("application/json")
    fun collectEvents(): EventCollection
}
