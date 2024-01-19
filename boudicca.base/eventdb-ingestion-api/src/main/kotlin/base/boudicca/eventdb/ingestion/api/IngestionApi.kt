package base.boudicca.eventdb.ingestion.api

import base.boudicca.model.Entry
import base.boudicca.model.Event
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Api("EventDB Ingestion")
@RequestMapping("/ingest")
interface IngestionApi {
    @ApiOperation("adds an event to the eventdb")
    @PostMapping(
        "/add",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Deprecated("use newer endpoint /ingest/entry")
    fun add(@RequestBody event: Event)

    @ApiOperation("adds an entry to the eventdb")
    @PostMapping(
        "/entry",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun addEntry(@RequestBody entry: Entry)

    @ApiOperation("adds multiple entries to the eventdb")
    @PostMapping(
        "/entries",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun addEntries(@RequestBody entries: List<Entry>)
}