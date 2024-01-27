package base.boudicca.api.eventdb

import base.boudicca.model.Entry
import base.boudicca.model.Event
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path

@OpenAPIDefinition
@Path("/ingest")
interface IngestionApi {
    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "adds an event to the eventdb",
                useReturnTypeSchema = true
            )
        ],
        tags = ["ingestion"]
    )
    @POST
    @Path("add")
    @Consumes("application/json")
    @Deprecated("use newer endpoint /ingest/entry")
    fun add(event: Event)

    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "adds an entry to the eventdb",
                useReturnTypeSchema = true
            )
        ],
        tags = ["ingestion"]
    )
    @POST
    @Path("entry")
    @Consumes("application/json")
    fun addEntry( entry: Entry)

    @Operation(
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "adds multiple entries to the eventdb",
                useReturnTypeSchema = true
            )
        ],
        tags = ["ingestion"]
    )
    @POST
    @Path("entries")
    @Consumes("application/json")
    fun addEntries( entries: List<Entry>)
}