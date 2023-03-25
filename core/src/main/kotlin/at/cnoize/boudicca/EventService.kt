package at.cnoize.boudicca

import java.time.Instant
import java.util.*
import java.util.stream.Collectors
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

class EventService {

    private val events = mutableSetOf<Event>()

    fun list(): Set<Event> {
        return events
    }

    fun add(event: Event) {
        events.add(event)
    }

    fun search(searchDTO: SearchDTO): Set<Event> {
        println(searchDTO)
        return events.stream()
                .filter { e -> searchDTO.fromDate == null || !e.startDate.isBefore(searchDTO.fromDate) }
                .filter { e -> searchDTO.toDate == null || !e.startDate.isAfter(searchDTO.toDate) }
                .filter { e -> searchDTO.name == null || e.name.contains(searchDTO.name!!) }
                .collect(Collectors.toSet())
    }

    init {
        events.add(Event(name = "TestEvent", startDate = Instant.now()))
        events.add(Event(name = "TestEvent2", startDate = Instant.now()))
        events.add(Event(name = "TestEvent3", startDate = Instant.now()))
        events.add(Event(name = "TestEvent4", startDate = Instant.now(), data = mapOf("key" to "value", "test" to "testvalue")))
    }
}
