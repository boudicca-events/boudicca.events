package at.cnoize.boudicca.publisherapi

import at.cnoize.boudicca.model.ComplexSearchDto
import at.cnoize.boudicca.model.Event
import at.cnoize.boudicca.model.SearchDTO
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@RegisterRestClient
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