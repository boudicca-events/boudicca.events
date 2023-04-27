import events.boudicca.CalendarService
import events.boudicca.model.ComplexSearchDto
import events.boudicca.model.SearchDTO
import org.jboss.resteasy.annotations.Query
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/calendar.ics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class IcalResource @Inject constructor(private val calendarService: CalendarService) {

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    open fun getAllEvents(@Query labels: String?): Response {
        val labelsSeparated = labels?.split(",")
        val calendarFile = calendarService.getEvents(labelsSeparated)
        val response: Response.ResponseBuilder = Response.ok(calendarFile as Any)
        response.header("Content-Disposition", "attachment;filename=$calendarFile")
        return response.build()

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
