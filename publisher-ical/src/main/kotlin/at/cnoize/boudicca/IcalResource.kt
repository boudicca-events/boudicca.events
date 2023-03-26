import at.cnoize.boudicca.CalendarService
import at.cnoize.boudicca.api.ComplexSearchDto
import at.cnoize.boudicca.api.SearchDTO
import io.smallrye.mutiny.Uni
import org.jboss.resteasy.reactive.RestQuery
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/calendar.ics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class IcalResource {

    private val calendarService = CalendarService()

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    open fun getAllEvents(@RestQuery labels: String?): Uni<Response?>? {
        val labelsSeparated = labels?.split(",")
        val calendarFile = calendarService.getEvents(labelsSeparated)
        val response: Response.ResponseBuilder = Response.ok(calendarFile as Any)
        response.header("Content-Disposition", "attachment;filename=$calendarFile")
        return Uni.createFrom().item(response.build())

    }

    @Path("search")
    @POST
    fun search(searchDTO: SearchDTO) {
        //return calendarService.search(searchDTO)
    }

    @Path("searchBy")
    @POST
    fun searchBy(complexSearchDto: ComplexSearchDto) {
        //return calendarService.searchBy(complexSearchDto)
    }
}
