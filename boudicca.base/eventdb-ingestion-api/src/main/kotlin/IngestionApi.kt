package base.boudicca.eventdb.controller

import base.boudicca.model.Entry
import base.boudicca.model.Event
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/ingest")
interface IngestionApi {
    @PostMapping(
        "/add",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("use newer endpoint /ingest/entry")
    fun add(@RequestBody event: Event)

    @PostMapping(
        "/entry",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun addEntry(@RequestBody entry: Entry)

    @PostMapping(
        "/entries",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun addEntries(@RequestBody entries: List<Entry>)
}