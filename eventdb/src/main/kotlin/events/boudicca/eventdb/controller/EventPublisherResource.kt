package events.boudicca.eventdb.controller

import events.boudicca.eventdb.model.ComplexSearchDto
import events.boudicca.eventdb.model.Event
import events.boudicca.eventdb.model.SearchDTO
import events.boudicca.eventdb.service.EventSearchService
import events.boudicca.eventdb.service.EventService
import jakarta.annotation.security.RolesAllowed
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/events")
class EventPublisherResource @Autowired constructor(
    private val eventService: EventService,
    private val eventSearchService: EventSearchService,
) {

    @GetMapping
    fun list(): Set<Event> {
        return eventService.list()
    }

    @PostMapping(
        "search",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun search(@RequestBody searchDTO: SearchDTO): Set<Event> {
        return eventSearchService.search(searchDTO)
    }

    @PostMapping(
        "searchBy",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun searchBy(@RequestBody complexSearchDto: ComplexSearchDto): Set<Event> {
        return eventSearchService.searchBy(complexSearchDto)
    }

}