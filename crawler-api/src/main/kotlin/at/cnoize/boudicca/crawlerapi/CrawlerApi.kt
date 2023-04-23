package at.cnoize.boudicca.crawlerapi

import at.cnoize.boudicca.model.Event
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.POST
import javax.ws.rs.Path

@RegisterRestClient
@Path("/crawler")
interface CrawlerApi {

    @POST
    @Path("add")
    fun add(event: Event)
}