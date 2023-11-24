package base.boudicca.eventdb.controller

import base.boudicca.model.Entry
import base.boudicca.model.Event
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Api("Ingestion")
@RequestMapping("/ingest")
interface IngestionApi {
    @ApiOperation("returns all entries from the event db")
    @PostMapping(
        "/add",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("use newer endpoint /ingest/entry")
    fun add(@RequestBody event: Event)

    @ApiOperation("returns all entries from the event db")

    @PostMapping(
        "/entry",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun addEntry(@RequestBody entry: Entry)

    @ApiOperation("returns all entries from the event db")

    @PostMapping(
        "/entries",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun addEntries(@RequestBody entries: List<Entry>)
}