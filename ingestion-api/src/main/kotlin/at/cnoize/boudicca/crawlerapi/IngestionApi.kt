package at.cnoize.boudicca.crawlerapi

import at.cnoize.boudicca.model.Event
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.POST
import javax.ws.rs.Path

@RegisterRestClient(configKey = "ingestion-api")
@ApplicationScoped
@Path("/ingest")
interface IngestionApi {

    @POST
    @Path("/add")
    fun add(event: Event)
}