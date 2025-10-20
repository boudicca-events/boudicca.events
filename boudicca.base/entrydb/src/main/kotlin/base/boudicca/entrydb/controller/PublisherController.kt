package base.boudicca.entrydb.controller

import base.boudicca.api.eventdb.PublisherApi
import base.boudicca.entrydb.service.EntryService
import base.boudicca.model.Entry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping()
class PublisherController @Autowired constructor(
    private val entryService: EntryService,
) : PublisherApi {

    @GetMapping("/entries")
    @ResponseBody
    override fun all(): Set<Entry> {
        return entryService.all()
    }

    @GetMapping("/entry/{boudiccaId}")
    @ResponseBody
    override fun entry(@PathVariable boudiccaId: UUID): Entry? {
        return entryService.get(boudiccaId)
    }
}
