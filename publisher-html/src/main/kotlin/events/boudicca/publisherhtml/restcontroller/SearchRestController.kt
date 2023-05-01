package events.boudicca.publisherhtml.restcontroller

import events.boudicca.openapi.model.Event
import events.boudicca.openapi.model.SearchDTO
import events.boudicca.publisherhtml.service.EventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/search")
class SearchRestController @Autowired constructor(private val eventService: EventService) {
    @GetMapping()
    @ResponseBody
    fun search(@RequestParam("name") name: String): ResponseEntity<Set<Event>> {
        val events = eventService.search(SearchDTO().name(name))
        return ResponseEntity(events, HttpStatus.OK)
    }
}





