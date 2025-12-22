package base.boudicca.entrydb.controller

import base.boudicca.api.eventdb.PublisherApi
import base.boudicca.entrydb.service.EntryService
import base.boudicca.model.Entry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping()
class PublisherController
@Autowired
constructor(private val entryService: EntryService) : PublisherApi {
    @GetMapping("/entries")
    @ResponseBody
    override fun all(): Set<Entry> = entryService.all()

    @GetMapping("/entry/{boudiccaId}")
    @ResponseBody
    override fun entry(@PathVariable boudiccaId: UUID): Entry? = entryService.get(boudiccaId)
}
