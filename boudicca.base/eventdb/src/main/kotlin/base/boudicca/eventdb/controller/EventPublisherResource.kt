package base.boudicca.eventdb.controller

import base.boudicca.eventdb.model.ComplexSearchDto
import base.boudicca.eventdb.model.Event
import base.boudicca.eventdb.model.SearchDTO
import base.boudicca.eventdb.service.EventSearchService
import base.boudicca.eventdb.service.EventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

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