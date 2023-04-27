package events.boudicca.publisherapi

import events.boudicca.model.ComplexSearchDto
import events.boudicca.model.Event
import events.boudicca.model.SearchDTO
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@RegisterRestClient(configKey = "publisher-api")
@ApplicationScoped
@Path("/events")
interface PublisherApi {

    @GET
    fun list(): Set<Event>;

    @POST
    @Path("/search")
    fun search(searchDTO: SearchDTO): Set<Event>;

    @POST
    @Path("/searchBy")
    fun searchBy(complexSearchDto: ComplexSearchDto): Set<Event>;
}