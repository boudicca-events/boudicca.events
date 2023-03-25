package at.cnoize.boudicca

import LinzTermineApi
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Path("/events")
class EventsResource {

        @Inject
        @RestClient
        lateinit var client: LinzTermineApi

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        fun getAllEvents(): List<Event> {
            val allEvents = client.getEvents().eventList
            return allEvents.map { Event(it.title, ZonedDateTime.from(it.start)) }
        }
}