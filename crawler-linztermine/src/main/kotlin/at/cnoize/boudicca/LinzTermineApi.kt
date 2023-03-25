import at.cnoize.boudicca.LinzTermineEvents
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.GET

@RegisterRestClient(baseUri = "https://www.linztermine.at/schnittstelle/downloads/events_xml.php")
interface LinzTermineApi {
    @GET
    fun getEvents(): LinzTermineEvents
}
