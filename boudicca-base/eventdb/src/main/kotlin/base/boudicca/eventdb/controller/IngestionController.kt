package base.boudicca.eventdb.controller

import base.boudicca.model.Entry
import base.boudicca.model.Event
import base.boudicca.eventdb.service.EntryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class IngestionController @Autowired constructor(private val entryService: EntryService) : IngestionApi {

    @Deprecated("use newer endpoint /ingest/entry")
    override fun add(@RequestBody event: Event) {
        entryService.add(Event.toEntry(event))
    }

    override fun addEntry(@RequestBody entry: Entry) {
        entryService.add(entry)
    }

    override fun addEntries(@RequestBody entries: List<Entry>) {
        for (entry in entries) {
            entryService.add(entry)
        }
    }

}