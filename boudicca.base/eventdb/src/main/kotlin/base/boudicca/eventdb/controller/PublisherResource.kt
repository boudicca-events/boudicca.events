package base.boudicca.eventdb.controller

import base.boudicca.Entry
import base.boudicca.eventdb.service.EntryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/entries")
class PublisherResource @Autowired constructor(
    private val entryService: EntryService,
) {

    @GetMapping("")
    fun all(): Set<Entry> {
        return entryService.all()
    }

}