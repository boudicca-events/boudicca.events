package base.boudicca.eventdb.controller

import base.boudicca.Entry
import base.boudicca.Event
import base.boudicca.eventdb.service.EntryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ingest")
class IngestionResource @Autowired constructor(private val entryService: EntryService) {

    @PostMapping(
        "/add",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("use newer endpoint /ingest/entry")
    fun add(@RequestBody event: Event) {
        entryService.add(Event.toEntry(event))
    }

    @PostMapping(
        "/entry",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun addEntry(@RequestBody event: Entry) {
        entryService.add(event)
    }

}