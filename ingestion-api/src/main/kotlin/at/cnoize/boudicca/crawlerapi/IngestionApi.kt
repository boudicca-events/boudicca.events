package at.cnoize.boudicca.crawlerapi

import at.cnoize.boudicca.model.Event
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/ingest")
interface IngestionApi {

    @POST
    @Path("/add")
    fun add(event: Event)
}