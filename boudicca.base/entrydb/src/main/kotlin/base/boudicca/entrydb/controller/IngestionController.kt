package base.boudicca.entrydb.controller

import base.boudicca.api.eventdb.IngestionApi
import base.boudicca.entrydb.service.EntryService
import base.boudicca.model.Entry
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/ingest")
class IngestionController(
    private val entryService: EntryService,
) : IngestionApi {
    @PostMapping(
        "/entry",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun addEntry(
        @RequestBody entry: Entry,
    ) {
        entryService.add(entry)
    }

    @PostMapping(
        "/entries",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun addEntries(
        @RequestBody entries: List<Entry>,
    ) {
        for (entry in entries) {
            entryService.add(entry)
        }
    }

    @PostMapping(
        "/duplicates",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun markDuplicates(duplicateIds: List<UUID>) {
        TODO("Not yet implemented")
    }
}
