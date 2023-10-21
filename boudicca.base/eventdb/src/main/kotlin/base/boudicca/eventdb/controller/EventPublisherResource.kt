package base.boudicca.eventdb.controller

import base.boudicca.Event
import base.boudicca.eventdb.model.ComplexSearchDto
import base.boudicca.eventdb.model.SearchDTO
import base.boudicca.eventdb.service.EventSearchService
import base.boudicca.eventdb.service.EntryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/events")
class EventPublisherResource @Autowired constructor(
    private val entryService: EntryService,
    private val eventSearchService: EventSearchService,
) {

    @GetMapping
    @Deprecated("use newer /entry endpoints")
    fun list(): Set<Event> {
        return entryService.all().mapNotNull { Event.fromEntry(it) }.toSet()
    }

    @PostMapping(
        "search",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("use the search service instead")
    fun search(@RequestBody searchDTO: SearchDTO): Set<Event> {
        return eventSearchService.search(searchDTO)
    }

    @PostMapping(
        "searchBy",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("use the search service instead")
    fun searchBy(@RequestBody complexSearchDto: ComplexSearchDto): Set<Event> {
        return eventSearchService.searchBy(complexSearchDto)
    }

}