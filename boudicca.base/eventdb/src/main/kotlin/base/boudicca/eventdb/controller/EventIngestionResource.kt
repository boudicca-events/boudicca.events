package base.boudicca.eventdb.controller

import base.boudicca.Event
import base.boudicca.eventdb.service.EventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ingest")
class EventIngestionResource @Autowired constructor(private val eventService: EventService) {

    @PostMapping(
        "/add",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun add(@RequestBody event: Event) {
        eventService.add(event)
    }

}