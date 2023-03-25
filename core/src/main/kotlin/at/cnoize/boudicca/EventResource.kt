package at.cnoize.boudicca

import java.time.Instant
import java.util.*
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/event")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResources {

    private val events = mutableSetOf<Event>()

    @GET
    fun list(): Set<Event> {
        return events
    }

    @POST
    fun add(event: Event) {
        events.add(event)
    }

    init {
        events.add(Event(name = "TestEvent", startDate = Instant.now()))
        events.add(Event(name = "TestEvent2", startDate = Instant.now()))
        events.add(Event(name = "TestEvent3", startDate = Instant.now()))
        events.add(Event(name = "TestEvent4", startDate = Instant.now(), data = mapOf("key" to "value", "test" to "testvalue")))
    }
}
