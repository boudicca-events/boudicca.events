package base.boudicca.eventdb.controller

import base.boudicca.api.eventdb.PublisherApi
import base.boudicca.eventdb.service.EntryService
import base.boudicca.model.Entry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/entries")
class PublisherController @Autowired constructor(
    private val entryService: EntryService,
) : PublisherApi {

    @GetMapping("")
    @ResponseBody
    override fun all(): Set<Entry> {
        return entryService.all()
    }

}
